/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.cleanroom.CleanroomAO;
import com.mytiki.account.features.latest.cleanroom.CleanroomAOReq;
import com.mytiki.account.features.latest.cleanroom.CleanroomDO;
import com.mytiki.account.features.latest.cleanroom.CleanroomService;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.oauth.OauthSubNamespace;
import com.mytiki.account.features.latest.ocean.*;
import com.mytiki.account.features.latest.org.OrgService;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileRepository;
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
    private CleanroomService cleanroomService;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private OrgService orgService;
    private OceanService service;
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
        this.service = new OceanService(sfnClient, s3Client, arn, mapper, repository);
    }

    @Test
    public void Test_Query_Success(){
        String query = "SELECT COUNT(*) FROM dummy";
        String name = "testCleanroom";

        ProfileDO testUser = new ProfileDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgService.create());
        testUser = profileRepository.save(testUser);

        CleanroomAOReq req = new CleanroomAOReq();
        req.setName(name);
        CleanroomAO createdCleanroom = cleanroomService.create(req,
                new OauthSub(OauthSubNamespace.USER, testUser.getUserId().toString()));
        Optional<CleanroomDO> cleanroom = cleanroomService.getDO(createdCleanroom.getCleanroomId());

        SubscriptionDO subscription = new SubscriptionDO();
        subscription.setStatus(SubscriptionStatus.ESTIMATE);
        subscription.setSubscriptionId(UUID.randomUUID());
        subscription.setModified(ZonedDateTime.now());
        subscription.setCreated(ZonedDateTime.now());
        subscription.setQuery(query);
        subscription.setCleanroom(cleanroom.get());
        subscription = subscriptionRepository.save(subscription);

        OceanDO rsp = service.query(subscription, OceanType.COUNT, query);
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
        String name = "testCleanroom";

        ProfileDO testUser = new ProfileDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgService.create());
        testUser = profileRepository.save(testUser);

        CleanroomAOReq cleanroomReq = new CleanroomAOReq();
        cleanroomReq.setName(name);
        CleanroomAO createdCleanroom = cleanroomService.create(cleanroomReq,
                new OauthSub(OauthSubNamespace.USER, testUser.getUserId().toString()));
        Optional<CleanroomDO> cleanroom = cleanroomService.getDO(createdCleanroom.getCleanroomId());

        SubscriptionDO subscription = new SubscriptionDO();
        subscription.setStatus(SubscriptionStatus.ESTIMATE);
        subscription.setSubscriptionId(UUID.randomUUID());
        subscription.setModified(ZonedDateTime.now());
        subscription.setCreated(ZonedDateTime.now());
        subscription.setQuery(query);
        subscription.setCleanroom(cleanroom.get());
        subscription = subscriptionRepository.save(subscription);

        OceanDO ocean = service.query(subscription, OceanType.COUNT, query);

        OceanAOReq req = new OceanAOReq(ocean.getRequestId().toString(), resultUri);
        service.update(req);

        Optional<OceanDO> found = repository.findByRequestId(ocean.getRequestId());
        assertTrue(found.isPresent());
        assertEquals(OceanStatus.SUCCESS, found.get().getStatus());
        assertEquals(resultUri, found.get().getResultUri());
        assertNotEquals(ocean.getModified(), found.get().getModified());
    }
}
