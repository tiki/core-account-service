/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;

import com.mytiki.account.features.latest.api_key.ApiKeyDO;
import com.mytiki.account.features.latest.api_key.ApiKeyRepository;
import com.mytiki.account.features.latest.api_key.ApiKeyService;
import com.mytiki.account.features.latest.oauth.OauthScopes;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.oauth.OauthSubNamespace;
import com.mytiki.account.features.latest.org.OrgRepository;
import com.mytiki.account.features.latest.org.OrgService;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileRepository;
import com.mytiki.account.features.latest.stripe.StripeService;
import com.mytiki.account.main.App;
import com.mytiki.account.mocks.JwtMock;
import com.mytiki.account.mocks.StripeMock;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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
public class ApiKeyTest {

    @Autowired
    private ApiKeyRepository repository;

    @Autowired
    private ApiKeyService service;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private OauthScopes allowedScopes;

    @Autowired
    private JWKSet jwkSet;

    @Autowired
    private OrgRepository orgRepository;

    private OrgService orgService;

    @BeforeAll
    public void before() throws StripeException {
        orgService = new OrgService(orgRepository, StripeMock.facade());
    }

    @Test
    public void Test_Create_Success() throws JOSEException {
        ProfileDO testUser = new ProfileDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgService.create("dummy@dummy.com"));
        testUser = profileRepository.save(testUser);
        String label = UUID.randomUUID().toString();
        ApiKeyDO key = service.create(testUser, label, allowedScopes.filter("account:admin"), 100L);
        assertNotNull(key.getId());
        assertNotNull(key.getCreated());
        assertNotNull(key.getToken());
        assertEquals(label, key.getLabel());
    }

    @Test
    public void Test_GetByEmail_Success() throws JOSEException {
        ProfileDO testUser = new ProfileDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgService.create("dummy@dummy.com"));
        testUser = profileRepository.save(testUser);
        String label = UUID.randomUUID().toString();
        ApiKeyDO key = service.create(testUser, label, allowedScopes.filter("account:admin"), 100L);

        List<ApiKeyDO> keys = repository.findAllByProfileEmail(testUser.getEmail());

        assertEquals(1, keys.size());
        assertEquals(label, keys.get(0).getLabel());
        assertEquals(key.getId(), keys.get(0).getId());
    }

    @Test
    @Transactional
    public void Test_Revoke_Success() throws JOSEException {
        ProfileDO testUser = new ProfileDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgService.create("dummy@dummy.com"));
        testUser = profileRepository.save(testUser);
        String label = UUID.randomUUID().toString();
        ApiKeyDO key = service.create(testUser, label, allowedScopes.filter("account:admin"), 100L);
        service.revoke(key.getToken());
        Optional<ApiKeyDO> found = repository.findById(key.getId());
        assertTrue(found.isEmpty());
    }

    @Test
    public void Test_Revoke_NoKey_Success() {
        service.revoke(UUID.randomUUID().toString());
    }

    @Test
    public void Test_Authorize_Success() throws JOSEException {
        ProfileDO testUser = new ProfileDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgService.create("dummy@dummy.com"));
        testUser = profileRepository.save(testUser);
        String label = UUID.randomUUID().toString();
        ApiKeyDO key = service.create(testUser, label, allowedScopes.filter("account:admin"), 100L);

        OAuth2AccessTokenResponse rsp = service.authorize(
                new OauthSub(OauthSubNamespace.USER, testUser.getUserId().toString() + ":" + UUID.randomUUID()),
                key.getToken(),
                allowedScopes.filter("account:admin"), 100L);

        JwtDecoder jwtDecoder = JwtMock.mockJwtDecoder(jwkSet);
        Jwt jwt = jwtDecoder.decode(rsp.getAccessToken().getTokenValue());
        assertNotNull(rsp.getAccessToken().getTokenValue());
        OauthSub sub = new OauthSub(jwt.getSubject());
        assertEquals(OauthSubNamespace.USER, sub.getNamespace());
        assertEquals(testUser.getUserId().toString(), sub.getId());
        assertTrue(rsp.getAccessToken().getScopes().contains("account:admin"));
    }
}
