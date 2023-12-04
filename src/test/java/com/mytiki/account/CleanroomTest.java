/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.cleanroom.CleanroomAO;
import com.mytiki.account.features.latest.cleanroom.CleanroomAOReq;
import com.mytiki.account.features.latest.cleanroom.CleanroomRepository;
import com.mytiki.account.features.latest.cleanroom.CleanroomService;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.oauth.OauthSubNamespace;
import com.mytiki.account.features.latest.ocean.OceanRepository;
import com.mytiki.account.features.latest.ocean.OceanService;
import com.mytiki.account.features.latest.org.OrgRepository;
import com.mytiki.account.features.latest.org.OrgService;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileRepository;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.main.App;
import com.mytiki.account.mocks.OceanMock;
import com.mytiki.account.mocks.StripeMock;
import com.mytiki.account.utilities.error.ApiException;
import com.mytiki.account.utilities.facade.StripeF;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CleanroomTest {
    @Autowired
    private OceanRepository oceanRepository;
    @Autowired
    private CleanroomRepository repository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private ObjectMapper mapper;
    private CleanroomService service;
    private String cleanroomId;
    private String userId;
    @Autowired
    private OrgRepository orgRepository;
    private OrgService orgService;

    @BeforeAll
    public void before() throws StripeException {
        StripeF stripe = StripeMock.facade();
        OceanService oceanService = new OceanService(
                OceanMock.aws(), "dummy", mapper, oceanRepository, stripe);
        service = new CleanroomService(repository, profileService, oceanService);
        orgService = new OrgService(orgRepository, stripe);
    }

    @Test
    @Order(1)
    public void Test_Create_Success() {
        String name = "testCleanroom";

        ProfileDO testUser = new ProfileDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgService.create("dummy@dummy.com"));
        testUser = profileRepository.save(testUser);
        userId = testUser.getUserId().toString();

        CleanroomAOReq req = new CleanroomAOReq();
        req.setName(name);
        CleanroomAO cleanroom = service.create(req,
                new OauthSub(OauthSubNamespace.USER, testUser.getUserId().toString()));
        cleanroomId = cleanroom.getCleanroomId();

        assertEquals(name, cleanroom.getName());
        assertNotNull(cleanroom.getCleanroomId());
        assertNotNull(cleanroom.getModified());
        assertNotNull(cleanroom.getCreated());
        assertEquals(testUser.getOrg().getOrgId().toString(), cleanroom.getOrgId());
        assertNull(cleanroom.getIam());
    }

    @Test
    @Order(2)
    public void Test_Update_Success() {
        String testIam = "aws::iam::test";
        CleanroomAOReq req = new CleanroomAOReq();
        req.setIam(List.of(testIam));
        CleanroomAO update = service.update(new OauthSub(OauthSubNamespace.USER, userId), cleanroomId, req);
        assertEquals(cleanroomId, update.getCleanroomId());
        assertEquals(1, update.getIam().size());
        assertEquals(testIam, update.getIam().get(0));
    }

    @Test
    @Order(3)
    public void Test_Get_Success() {
        CleanroomAO cleanroom = service.get(new OauthSub(OauthSubNamespace.USER, userId), cleanroomId);
        assertEquals(cleanroomId, cleanroom.getCleanroomId());
        assertEquals(1, cleanroom.getIam().size());
        assertNotNull(cleanroom.getCleanroomId());
        assertNotNull(cleanroom.getModified());
        assertNotNull(cleanroom.getCreated());
    }

    @Test
    @Order(4)
    @Transactional
    public void Test_Delete_Success() {
        service.delete(new OauthSub(OauthSubNamespace.USER, userId), cleanroomId);
        assertThrows(ApiException.class, () -> service.get(new OauthSub(OauthSubNamespace.USER, userId), cleanroomId));
    }
}
