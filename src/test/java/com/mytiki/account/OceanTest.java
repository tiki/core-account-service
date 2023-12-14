/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.cleanroom.*;
import com.mytiki.account.features.latest.ocean.*;
import com.mytiki.account.features.latest.profile.ProfileRepository;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.features.latest.subscription.SubscriptionRepository;
import com.mytiki.account.main.App;
import com.mytiki.account.mocks.JwtMock;
import com.mytiki.account.mocks.OceanMock;
import com.mytiki.account.mocks.StripeMock;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class, JwtMock.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OceanTest {
    @Value("${com.mytiki.account.ocean.arn.state}")
    private String arn;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private OceanRepository repository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private CleanroomRepository cleanroomRepository;
    @Autowired
    private ProfileService profileService;
    private OceanService service;
    private CleanroomService cleanroomService;
    private final String executionArn = "dummy-execution-arn";

    @BeforeEach
    public void before() throws StripeException {
        OceanSF sf = OceanMock.sf(executionArn, arn, mapper);
        this.service = new OceanService(sf, OceanMock.lf(), "dummy", mapper, repository, StripeMock.facade());
        this.cleanroomService = new CleanroomService(cleanroomRepository, profileService, this.service);
    }

    @Test
    public void Test_Query_Success(){
        String query = "SELECT COUNT(*) FROM dummy";
        OceanDO rsp = service.count(query);
        assertNotNull(rsp.getCreated());
        assertNotNull(rsp.getModified());
        assertNotNull(rsp.getRequestId());
        assertNotNull(rsp.getId());
        assertEquals(OceanType.COUNT, rsp.getType());
        assertEquals(executionArn, rsp.getExecutionArn());
        assertEquals(OceanStatus.PENDING, rsp.getStatus());
    }

    @Test
    public void Test_Update_Success(){
        String resultUri = "dummy://";
        String query = "SELECT COUNT(*) FROM dummy";

        OceanDO ocean = service.count(query);
        OceanAOReq req = new OceanAOReq(ocean.getRequestId().toString(), resultUri);
        service.callback(req);

        Optional<OceanDO> found = repository.findByRequestId(ocean.getRequestId());
        assertTrue(found.isPresent());
        assertEquals(OceanStatus.SUCCESS, found.get().getStatus());
        assertEquals(resultUri, found.get().getResultUri());
        assertNotEquals(ocean.getModified(), found.get().getModified());
    }
}
