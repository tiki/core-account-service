/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;

import com.mytiki.account.features.latest.api_key.*;
import com.mytiki.account.features.latest.app_info.AppInfoAO;
import com.mytiki.account.features.latest.app_info.AppInfoService;
import com.mytiki.account.features.latest.org_info.OrgInfoService;
import com.mytiki.account.features.latest.user_info.UserInfoDO;
import com.mytiki.account.features.latest.user_info.UserInfoRepository;
import com.mytiki.account.main.App;
import com.mytiki.account.mocks.JwtMock;
import com.mytiki.account.security.oauth.OauthSub;
import com.mytiki.account.security.oauth.OauthSubNamespace;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

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
    private UserInfoRepository userInfoRepository;

    @Autowired
    private AppInfoService appInfoService;

    @Autowired
    @Qualifier("mockJwtDecoder")
    private JwtDecoder jwtDecoder;

    @Autowired
    private OrgInfoService orgInfoService;

    @Test
    public void Test_Create_Success() {
        UserInfoDO testUser = new UserInfoDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgInfoService.create());
        testUser = userInfoRepository.save(testUser);
        AppInfoAO app = appInfoService.create("testApp", testUser.getUserId().toString());

        ApiKeyAOCreate key = service.create(app.getAppId(), true);
        assertNotNull(key.getId());
        assertNotNull(key.getCreated());
        assertNull(key.getSecret());
    }

    @Test
    public void Test_Get_Success() {
        UserInfoDO testUser = new UserInfoDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgInfoService.create());
        testUser = userInfoRepository.save(testUser);
        AppInfoAO app = appInfoService.create("testApp", testUser.getUserId().toString());

        ApiKeyAOCreate created = service.create(app.getAppId(), true);
        List<ApiKeyAO> found = service.getByAppId(app.getAppId());

        assertEquals(1, found.size());
        assertEquals(created.getId(), found.get(0).getId());
    }

    @Test
    public void Test_Revoke_Success() {
        UserInfoDO testUser = new UserInfoDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgInfoService.create());
        testUser = userInfoRepository.save(testUser);
        AppInfoAO app = appInfoService.create("testApp", testUser.getUserId().toString());

        ApiKeyAOCreate key = service.create(app.getAppId(), true);
        service.revoke(app.getAppId(), key.getId());

        Optional<ApiKeyDO> found = repository.findById(UUID.fromString(key.getId()));
        assertTrue(found.isEmpty());
    }

    @Test
    public void Test_Revoke_NoKey_Success() {
        service.revoke(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    @Test
    public void Test_Create_WithSecret_Success() {
        UserInfoDO testUser = new UserInfoDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgInfoService.create());
        testUser = userInfoRepository.save(testUser);
        AppInfoAO app = appInfoService.create("testApp", testUser.getUserId().toString());

        ApiKeyAOCreate key = service.create(app.getAppId(), false);
        assertNotNull(key.getId());
        assertNotNull(key.getCreated());
        assertNotNull(key.getSecret());

        Optional<ApiKeyDO> found = repository.findById(UUID.fromString(key.getId()));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        assertTrue(found.isPresent());
        assertTrue(encoder.matches(key.getSecret(), found.get().getHashedSecret()));
    }

    @Test
    public void Test_Authorize_Success() {
        UserInfoDO testUser = new UserInfoDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgInfoService.create());
        testUser = userInfoRepository.save(testUser);
        AppInfoAO app = appInfoService.create("testApp", testUser.getUserId().toString());
        ApiKeyAOCreate created = service.create(app.getAppId(), true);

        String scope = "account:app";
        OAuth2AccessTokenResponse rsp = service.authorize(created.getId(), created.getSecret(), scope);
        Jwt jwt = jwtDecoder.decode(rsp.getAccessToken().getTokenValue());
        assertNotNull(rsp.getAccessToken().getTokenValue());
        OauthSub sub = new OauthSub(jwt.getSubject());
        assertEquals(OauthSubNamespace.APP, sub.getNamespace());
        assertEquals(app.getAppId(), sub.getId());
        assertTrue(rsp.getAccessToken().getScopes().contains(scope));
    }

    @Test
    public void Test_Authorize_Secret_Success() {
        UserInfoDO testUser = new UserInfoDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgInfoService.create());
        testUser = userInfoRepository.save(testUser);
        AppInfoAO app = appInfoService.create("testApp", testUser.getUserId().toString());
        ApiKeyAOCreate created = service.create(app.getAppId(), false);

        OAuth2AccessTokenResponse rsp = service.authorize(created.getId(), created.getSecret(), null);
        Jwt jwt = jwtDecoder.decode(rsp.getAccessToken().getTokenValue());
        assertNotNull(rsp.getAccessToken().getTokenValue());
        OauthSub sub = new OauthSub(jwt.getSubject());
        assertEquals(OauthSubNamespace.APP, sub.getNamespace());
        assertEquals(app.getAppId(), sub.getId());
    }

    @Test
    public void Test_Authorize_Private_Success() {
        UserInfoDO testUser = new UserInfoDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgInfoService.create());
        testUser = userInfoRepository.save(testUser);
        AppInfoAO app = appInfoService.create("testApp", testUser.getUserId().toString());
        ApiKeyAOCreate created = service.create(app.getAppId(), true);

        String scope = "auth";
        OAuth2AccessTokenResponse rsp = service.authorize(created.getId(), created.getSecret(), scope);
        Jwt jwt = jwtDecoder.decode(rsp.getAccessToken().getTokenValue());
        assertNotNull(rsp.getAccessToken().getTokenValue());
        OauthSub sub = new OauthSub(jwt.getSubject());
        assertEquals(OauthSubNamespace.APP, sub.getNamespace());
        assertEquals(app.getAppId(), sub.getId());
        assertFalse(rsp.getAccessToken().getScopes().contains(scope));
    }

    @Test
    public void Test_Authorize_BadSecret_Failure() {
        UserInfoDO testUser = new UserInfoDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgInfoService.create());
        testUser = userInfoRepository.save(testUser);
        AppInfoAO app = appInfoService.create("testApp", testUser.getUserId().toString());
        ApiKeyAOCreate created = service.create(app.getAppId(), false);

        OAuth2AuthorizationException ex = assertThrows(OAuth2AuthorizationException.class,
                () -> service.authorize(created.getId(), UUID.randomUUID().toString(), null));
        assertEquals(ex.getError().getErrorCode(), OAuth2ErrorCodes.ACCESS_DENIED);
    }

    @Test
    public void Test_Authorize_BadId_Failure() {
        UserInfoDO testUser = new UserInfoDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgInfoService.create());
        testUser = userInfoRepository.save(testUser);
        AppInfoAO app = appInfoService.create("testApp", testUser.getUserId().toString());
        ApiKeyAOCreate created = service.create(app.getAppId(), false);

        OAuth2AuthorizationException ex = assertThrows(OAuth2AuthorizationException.class,
                () -> service.authorize(UUID.randomUUID().toString(), created.getId(), null));
        assertEquals(ex.getError().getErrorCode(), OAuth2ErrorCodes.ACCESS_DENIED);
    }
}
