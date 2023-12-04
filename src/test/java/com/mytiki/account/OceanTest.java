/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.cleanroom.*;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.oauth.OauthSubNamespace;
import com.mytiki.account.features.latest.ocean.*;
import com.mytiki.account.features.latest.org.OrgService;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileRepository;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.features.latest.subscription.SubscriptionDO;
import com.mytiki.account.features.latest.subscription.SubscriptionRepository;
import com.mytiki.account.features.latest.subscription.SubscriptionStatus;
import com.mytiki.account.main.App;
import com.mytiki.account.mocks.JwtMock;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.*;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class, JwtMock.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OceanTest {
    @Value("${com.mytiki.account.ocean.arn}")
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
    @Autowired
    private OrgService orgService;
    private OceanService service;
    private CleanroomService cleanroomService;
    private final String executionArn = "dummy-execution-arn";

    @BeforeEach
    public void before() {
        SfnClient sfnClient = Mockito.mock(SfnClient.class);
        Mockito.doReturn(StartExecutionResponse.builder().executionArn(executionArn).build())
                .when(sfnClient)
                .startExecution(Mockito.any(StartExecutionRequest.class));
        Mockito.doReturn(DescribeExecutionResponse.builder().status(ExecutionStatus.SUCCEEDED).build())
                .when(sfnClient)
                .describeExecution(Mockito.any(DescribeExecutionRequest.class));
        S3Client s3Client = Mockito.mock(S3Client.class);
        Mockito.doReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(),
                        "hello,world".getBytes(StandardCharsets.UTF_8)))
                .when(s3Client)
                .getObjectAsBytes(Mockito.any(GetObjectRequest.class));
        OceanAws oceanAws = new OceanAws(sfnClient, s3Client, arn, mapper);
        this.service = new OceanService(oceanAws, "dummy", mapper, repository);
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
