/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.api_key.ApiKeyDO;
import com.mytiki.account.features.latest.api_key.ApiKeyRepository;
import com.mytiki.account.features.latest.api_key.ApiKeyService;
import com.mytiki.account.features.latest.oauth.OauthScopes;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.oauth.OauthSubNamespace;
import com.mytiki.account.features.latest.ocean.*;
import com.mytiki.account.features.latest.org.OrgService;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileRepository;
import com.mytiki.account.main.App;
import com.mytiki.account.mocks.JwtMock;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.*;

import java.time.ZonedDateTime;
import java.util.List;
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
    private OceanService service;
    private final String executionArn = "dummy-execution-arn";

    @BeforeEach
    public void before() {
        SfnClient client = Mockito.mock(SfnClient.class);
        Mockito.doReturn(StartExecutionResponse.builder().executionArn(executionArn).build())
                .when(client)
                .startExecution(Mockito.any(StartExecutionRequest.class));
        Mockito.doReturn(DescribeExecutionResponse.builder().status(ExecutionStatus.SUCCEEDED).build())
                .when(client)
                .describeExecution(Mockito.any(DescribeExecutionRequest.class));
        this.service = new OceanService(client, arn, mapper, repository);
    }

    @Test
    public void Test_Query_Success(){
        OceanDO rsp = service.query(OceanType.COUNT, "SELECT COUNT(*) FROM dummy");
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
        OceanDO query = service.query(OceanType.COUNT, "SELECT COUNT(*) FROM dummy");

        OceanAO req = new OceanAO(query.getRequestId().toString(), resultUri);
        service.update(req);

        Optional<OceanDO> found = repository.findByRequestId(query.getRequestId());
        assertTrue(found.isPresent());
        assertEquals(OceanStatus.SUCCESS, found.get().getStatus());
        assertEquals(resultUri, found.get().getResultUri());
        assertNotEquals(query.getModified(), found.get().getModified());
    }
}
