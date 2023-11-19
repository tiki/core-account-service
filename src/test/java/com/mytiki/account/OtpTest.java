/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;

import com.mytiki.account.features.latest.oauth.OauthScopes;
import com.mytiki.account.features.latest.otp.OtpAOStartReq;
import com.mytiki.account.features.latest.otp.OtpAOStartRsp;
import com.mytiki.account.features.latest.otp.OtpRepository;
import com.mytiki.account.features.latest.otp.OtpService;
import com.mytiki.account.features.latest.profile.ProfileAO;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.main.App;
import com.mytiki.account.mocks.JwtMock;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.utilities.error.ApiException;
import com.mytiki.account.utilities.facade.SendgridF;
import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class, JwtMock.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OtpTest {

    @MockBean
    private SendgridF mockSendgrid;

    @Autowired
    private OtpService service;

    @Autowired
    private OtpRepository repository;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private OauthScopes allowedScopes;

    @Autowired
    private JWKSet jwkSet;

    @Test
    public void Test_Start_Success() {
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        OtpAOStartReq req = new OtpAOStartReq("test@test.com");
        OtpAOStartRsp rsp = service.start(req);

        assertNotNull(rsp.getDeviceId());
        assertNotNull(rsp.getExpires());
    }

    @Test
    public void Test_Start_Send_Failure() {
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), anyString())).thenReturn(false);

        OtpAOStartReq req = new OtpAOStartReq("test@test.com");

        ApiException ex = assertThrows(ApiException.class,
                () -> service.start(req));
        assertEquals(ex.getHttpStatus(), HttpStatus.EXPECTATION_FAILED);
    }

    @Test
    public void Test_Authorize_Bad_DID_Failure() {
        ArgumentCaptor<String> param = ArgumentCaptor.forClass(String.class);
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), param.capture())).thenReturn(true);

        OtpAOStartReq req = new OtpAOStartReq("test@test.com");
        service.start(req);
        String code = param.getValue().substring(0, 6);

        OAuth2AuthorizationException ex = assertThrows(OAuth2AuthorizationException.class,
                () -> service.authorize(UUID.randomUUID().toString(), code, null));
        assertEquals(ex.getError().getErrorCode(), OAuth2ErrorCodes.ACCESS_DENIED);
    }

    @Test
    public void Test_Authorize_Bad_Code_Failure() {
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        OtpAOStartReq req = new OtpAOStartReq("test@test.com");
        OtpAOStartRsp rsp = service.start(req);

        OAuth2AuthorizationException ex = assertThrows(OAuth2AuthorizationException.class,
                () -> service.authorize(rsp.getDeviceId(), UUID.randomUUID().toString(), null));
        assertEquals(ex.getError().getErrorCode(), OAuth2ErrorCodes.ACCESS_DENIED);
    }

    @Test
    public void Test_Authorize_Success() {
        ArgumentCaptor<String> param = ArgumentCaptor.forClass(String.class);
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), param.capture())).thenReturn(true);
        String testEmail = UUID.randomUUID() + "@test.com";
        OtpAOStartReq req = new OtpAOStartReq(testEmail);
        OtpAOStartRsp rsp = service.start(req);

        String code = param.getValue().substring(0, 6);
        OAuth2AccessTokenResponse token = service.authorize(rsp.getDeviceId(), code, allowedScopes);

        assertNotNull(token.getAccessToken().getTokenValue());
        assertEquals(OAuth2AccessToken.TokenType.BEARER, token.getAccessToken().getTokenType());
        assertNotNull(token.getRefreshToken());
        assertNotNull(token.getRefreshToken().getTokenValue());
        assertNotNull(token.getAccessToken().getExpiresAt());
        assertTrue(token.getAccessToken().getExpiresAt().isAfter(Instant.now()));

        Jwt jwt = JwtMock.mockJwtDecoder(jwkSet).decode(token.getAccessToken().getTokenValue());
        OauthSub sub = new OauthSub(jwt.getSubject());
        assertTrue(sub.isUser());
        ProfileAO userInfo = profileService.get(sub.getId());
        assertEquals(testEmail, userInfo.getEmail());
    }

    @Test
    public void Test_Authorize_Login_Success() {
        ArgumentCaptor<String> param = ArgumentCaptor.forClass(String.class);
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), param.capture())).thenReturn(true);

        String testEmail = UUID.randomUUID() + "@test.com";
        OtpAOStartReq req = new OtpAOStartReq(testEmail);
        OtpAOStartRsp rsp = service.start(req);

        String code = param.getValue().substring(0, 6);
        OAuth2AccessTokenResponse token = service.authorize(rsp.getDeviceId(), code, allowedScopes);

        Jwt jwt = JwtMock.mockJwtDecoder(jwkSet).decode(token.getAccessToken().getTokenValue());
        OauthSub sub = new OauthSub(jwt.getSubject());
        assertTrue(sub.isUser());
        ProfileAO userInfo = profileService.get(sub.getId());
        assertEquals(testEmail, userInfo.getEmail());
    }

    @Test
    public void Test_Authorize_Audience_Success() {
        ArgumentCaptor<String> param = ArgumentCaptor.forClass(String.class);
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), param.capture())).thenReturn(true);
        String testEmail = UUID.randomUUID() + "@test.com";
        OtpAOStartReq req = new OtpAOStartReq(testEmail);
        OtpAOStartRsp rsp = service.start(req);

        String code = param.getValue().substring(0, 6);
        OAuth2AccessTokenResponse token = service.authorize(
                rsp.getDeviceId(), code, allowedScopes.filter("trail"));

        Jwt jwt = JwtMock.mockJwtDecoder(jwkSet).decode(token.getAccessToken().getTokenValue());
        assertTrue(jwt.getAudience().contains("trail.mytiki.com"));
        assertTrue(token.getAccessToken().getScopes().contains("trail"));
    }
}
