/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.oauth.OauthSubNamespace;
import com.mytiki.account.features.latest.org.OrgRepository;
import com.mytiki.account.features.latest.org.OrgService;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileRepository;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.features.latest.provider.ProviderAO;
import com.mytiki.account.features.latest.provider.ProviderRepository;
import com.mytiki.account.features.latest.provider.ProviderService;
import com.mytiki.account.main.App;
import com.mytiki.account.mocks.SqsMock;
import com.mytiki.account.mocks.StripeMock;
import com.mytiki.account.utilities.error.ApiException;
import com.mytiki.account.utilities.facade.SqsF;
import com.nimbusds.jose.JWSSigner;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProviderTest {

    @Autowired
    private ProviderRepository repository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private JWSSigner jwsSigner;

    @Autowired
    private OrgRepository orgRepository;

    @Autowired
    private ObjectMapper mapper;

    private OrgService orgService;
    private ProviderService service;

    @BeforeAll
    public void before() throws StripeException {
        orgService = new OrgService(orgRepository, StripeMock.facade());
        SqsF trail = new SqsF(SqsMock.mock("dummy"), "dummy");
        service = new ProviderService(repository, profileService, jwsSigner, trail, mapper);
    }

    @Test
    public void Test_Create_Success() {
        String name = "testApp";

        ProfileDO testUser = new ProfileDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgService.create("dummy@dummy.com"));
        testUser = profileRepository.save(testUser);

        ProviderAO app = service.create(name, new OauthSub(OauthSubNamespace.USER, testUser.getUserId().toString()));
        assertEquals(name, app.getName());
        assertNotNull(app.getProviderId());
        assertNotNull(app.getModified());
        assertNotNull(app.getCreated());
        assertEquals(app.getOrgId(), testUser.getOrg().getOrgId().toString());
    }

    @Test
    public void Test_Create_NoUser_Failure() {
        String name = "testApp";

        ProfileDO testUser = new ProfileDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());

        ApiException ex = assertThrows(ApiException.class,
                () -> service.create(name, new OauthSub(OauthSubNamespace.USER, testUser.getUserId().toString())));
        assertNotNull(ex);
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
    }

    @Test
    public void Test_Get_Success() {
        String name = "testApp";

        ProfileDO testUser = new ProfileDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgService.create("dummy@dummy.com"));
        testUser = profileRepository.save(testUser);

        ProviderAO app = service.create(name, new OauthSub(OauthSubNamespace.USER, testUser.getUserId().toString()));
        ProviderAO found = service.get(app.getProviderId());

        assertEquals(app.getProviderId(), found.getProviderId());
        assertEquals(app.getName(), found.getName());
        assertEquals(app.getOrgId(), found.getOrgId());
    }

    @Test
    public void Test_Get_NoApp_Success() {
        String appId = UUID.randomUUID().toString();
        ProviderAO found = service.get(appId);
        assertNull(found);
    }
}
