/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth;

import com.mytiki.l0_auth.features.latest.otp.OtpAOStartReq;
import com.mytiki.l0_auth.features.latest.otp.OtpAOStartRsp;
import com.mytiki.l0_auth.features.latest.otp.OtpRepository;
import com.mytiki.l0_auth.features.latest.otp.OtpService;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoAO;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoService;
import com.mytiki.l0_auth.main.App;
import com.mytiki.l0_auth.utilities.Sendgrid;
import com.mytiki.spring_rest_api.ApiException;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OtpTest {

    @MockBean
    private Sendgrid mockSendgrid;

    @Autowired
    private OtpService service;

    @Autowired
    private OtpRepository repository;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    public void Test_Start_Success() {
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        OtpAOStartReq req = new OtpAOStartReq("test@test.com", false);
        OtpAOStartRsp rsp = service.start(req);

        assertNotNull(rsp.getDeviceId());
        assertNotNull(rsp.getExpires());
    }

    @Test
    public void Test_Start_Send_Failure() {
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), anyString())).thenReturn(false);

        OtpAOStartReq req = new OtpAOStartReq("test@test.com", false);

        ApiException ex = assertThrows(ApiException.class,
                () -> service.start(req));
        assertEquals(ex.getHttpStatus(), HttpStatus.EXPECTATION_FAILED);
    }

    @Test
    public void Test_Authorize_Success() {
        ArgumentCaptor<String> param = ArgumentCaptor.forClass(String.class);
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), param.capture())).thenReturn(true);
        OtpAOStartReq req = new OtpAOStartReq("test@test.com", false);
        OtpAOStartRsp rsp = service.start(req);

        String code = param.getValue().substring(19, 25);
        OAuth2AccessTokenResponse token = service.authorize(rsp.getDeviceId(), code, null);

        assertNotNull(token.getAccessToken().getTokenValue());
        assertEquals(OAuth2AccessToken.TokenType.BEARER, token.getAccessToken().getTokenType());
        assertNotNull(token.getRefreshToken());
        assertNotNull(token.getRefreshToken().getTokenValue());
        assertNotNull(token.getAccessToken().getExpiresAt());
        assertTrue(token.getAccessToken().getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    public void Test_Authorize_Bad_DID_Failure() {
        ArgumentCaptor<String> param = ArgumentCaptor.forClass(String.class);
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), param.capture())).thenReturn(true);

        OtpAOStartReq req = new OtpAOStartReq("test@test.com", false);
        service.start(req);
        String code = param.getValue().substring(19, 25);

        OAuth2AuthorizationException ex = assertThrows(OAuth2AuthorizationException.class,
                () -> service.authorize(UUID.randomUUID().toString(), code, null));
        assertEquals(ex.getError().getErrorCode(), OAuth2ErrorCodes.ACCESS_DENIED);
    }

    @Test
    public void Test_Authorize_Bad_Code_Failure() {
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        OtpAOStartReq req = new OtpAOStartReq("test@test.com", false);
        OtpAOStartRsp rsp = service.start(req);

        OAuth2AuthorizationException ex = assertThrows(OAuth2AuthorizationException.class,
                () -> service.authorize(rsp.getDeviceId(), UUID.randomUUID().toString(), null));
        assertEquals(ex.getError().getErrorCode(), OAuth2ErrorCodes.ACCESS_DENIED);
    }

    @Test
    public void Test_Authorize_Not_Anonymous_Success() {
        ArgumentCaptor<String> param = ArgumentCaptor.forClass(String.class);
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), param.capture())).thenReturn(true);
        String testEmail = UUID.randomUUID() + "@test.com";
        OtpAOStartReq req = new OtpAOStartReq(testEmail, true);
        OtpAOStartRsp rsp = service.start(req);

        String code = param.getValue().substring(19, 25);
        OAuth2AccessTokenResponse token = service.authorize(rsp.getDeviceId(), code, null);

        assertNotNull(token.getAccessToken().getTokenValue());
        assertEquals(OAuth2AccessToken.TokenType.BEARER, token.getAccessToken().getTokenType());
        assertNotNull(token.getRefreshToken());
        assertNotNull(token.getRefreshToken().getTokenValue());
        assertNotNull(token.getAccessToken().getExpiresAt());
        assertTrue(token.getAccessToken().getExpiresAt().isAfter(Instant.now()));

        Jwt jwt = jwtDecoder.decode(token.getAccessToken().getTokenValue());
        UserInfoAO userInfo = userInfoService.get(jwt.getSubject());
        assertEquals(testEmail, userInfo.getEmail());
    }

    @Test
    public void Test_Authorize_Not_Anonymous_Login_Success() {
        ArgumentCaptor<String> param = ArgumentCaptor.forClass(String.class);
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), param.capture())).thenReturn(true);

        String testEmail = UUID.randomUUID() + "@test.com";
        OtpAOStartReq req = new OtpAOStartReq(testEmail, true);
        OtpAOStartRsp rsp = service.start(req);

        String code = param.getValue().substring(19, 25);
        OAuth2AccessTokenResponse token = service.authorize(rsp.getDeviceId(), code, null);

        req = new OtpAOStartReq(testEmail, true);
        rsp = service.start(req);

        code = param.getValue().substring(19, 25);
        token = service.authorize(rsp.getDeviceId(), code, null);

        Jwt jwt = jwtDecoder.decode(token.getAccessToken().getTokenValue());
        UserInfoAO userInfo = userInfoService.get(jwt.getSubject());
        assertEquals(testEmail, userInfo.getEmail());
    }

    @Test
    public void Test_Authorize_Audience_Success() {
        ArgumentCaptor<String> param = ArgumentCaptor.forClass(String.class);
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), param.capture())).thenReturn(true);
        String testEmail = UUID.randomUUID() + "@test.com";
        OtpAOStartReq req = new OtpAOStartReq(testEmail, true);
        OtpAOStartRsp rsp = service.start(req);

        String scope = "storage";
        String code = param.getValue().substring(19, 25);
        OAuth2AccessTokenResponse token = service.authorize(rsp.getDeviceId(), code, scope);

        Jwt jwt = jwtDecoder.decode(token.getAccessToken().getTokenValue());
        assertTrue(jwt.getAudience().contains("storage.l0.mytiki.com"));
        assertTrue(token.getAccessToken().getScopes().contains(scope));
    }

    @Test
    public void Test_Authorize_Audience_Anonymous_Success() {
        ArgumentCaptor<String> param = ArgumentCaptor.forClass(String.class);
        Mockito.when(mockSendgrid.send(anyString(), anyString(), anyString(), param.capture())).thenReturn(true);
        String testEmail = UUID.randomUUID() + "@test.com";
        OtpAOStartReq req = new OtpAOStartReq(testEmail, false);
        OtpAOStartRsp rsp = service.start(req);

        String scope = "storage";
        String code = param.getValue().substring(19, 25);

        OAuth2AccessTokenResponse token = service.authorize(rsp.getDeviceId(), code, scope);
        assertFalse(token.getAccessToken().getScopes().contains(scope));
    }
}
