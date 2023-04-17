/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.api_key;

import com.mytiki.l0_auth.features.latest.app_info.AppInfoDO;
import com.mytiki.l0_auth.features.latest.app_info.AppInfoService;
import com.mytiki.l0_auth.features.latest.refresh.RefreshService;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoDO;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoService;
import com.mytiki.l0_auth.security.JWSBuilder;
import com.mytiki.l0_auth.security.OauthScope;
import com.mytiki.l0_auth.security.OauthScopes;
import com.mytiki.l0_auth.utilities.Constants;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.*;

public class ApiKeyService {
    private final ApiKeyRepository repository;
    private final UserInfoService userInfoService;
    private final AppInfoService appInfoService;
    private final RefreshService refreshService;
    private final JWSSigner signer;
    private final PasswordEncoder secretEncoder;
    private final OauthScopes allowedScopes;
    private final List<String> publicScopes;

    public ApiKeyService(
            ApiKeyRepository repository,
            UserInfoService userInfoService,
            AppInfoService appInfoService,
            RefreshService refreshService,
            JWSSigner signer,
            OauthScopes allowedScopes,
            List<String> publicScopes) {
        this.repository = repository;
        this.userInfoService = userInfoService;
        this.appInfoService = appInfoService;
        this.refreshService = refreshService;
        this.signer = signer;
        this.secretEncoder = new BCryptPasswordEncoder(12);
        this.allowedScopes = allowedScopes;
        this.publicScopes = publicScopes;
    }

    @Transactional
    public ApiKeyAOCreate create(String userId, String appId, boolean isPublic){
        String secret = null;
        guardAppUser(userId, appId);
        Optional<AppInfoDO> app = appInfoService.getDO(appId);
        if(app.isEmpty())
            throw new ApiExceptionBuilder(HttpStatus.BAD_REQUEST)
                    .message("Invalid App")
                    .help("Get valid appIds from ../oauth/userinfo")
                    .build();

        ApiKeyDO apiKey = new ApiKeyDO();
        apiKey.setId(UUID.randomUUID());
        apiKey.setApp(app.get());
        apiKey.setCreated(ZonedDateTime.now());

        if(!isPublic) {
            secret = generateSecret(32);
            apiKey.setHashedSecret(secretEncoder.encode(secret));
        }

        repository.save(apiKey);

        ApiKeyAOCreate rsp = new ApiKeyAOCreate();
        rsp.setId(apiKey.getId().toString());
        rsp.setCreated(apiKey.getCreated());
        rsp.setSecret(secret);
        return rsp;
    }

    @Transactional
    public List<ApiKeyAO> getByAppId(String userId, String appId){
        guardAppUser(userId, appId);
        List<ApiKeyDO> keys = repository.findAllByAppAppId(UUID.fromString(appId));
        return keys.stream().map(key -> {
            ApiKeyAO rsp = new ApiKeyAO();
            rsp.setId(key.getId().toString());
            rsp.setCreated(key.getCreated());
            rsp.setPublic(key.getHashedSecret() == null);
            return rsp;
        }).toList();
    }

    @Transactional
    public void revoke(String userId, String keyId){
        Optional<ApiKeyDO> found = repository.findById(UUID.fromString(keyId));
        if(found.isPresent()){
            guardAppUser(userId, found.get().getApp().getAppId().toString());
            repository.delete(found.get());
        }
    }

    public OAuth2AccessTokenResponse authorize(String clientId, String clientSecret, String requestScopes){
        Optional<ApiKeyDO> found = repository.findById(UUID.fromString(clientId));
        if (found.isEmpty())
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.ACCESS_DENIED,
                    "client_id and/or client_secret are invalid",
                    null
            ));
        try {
            Map<String, OauthScope> scopes = allowedScopes.parse(requestScopes);
            if(found.get().getHashedSecret() == null && clientSecret == null){
                scopes = allowedScopes.filter(scopes, publicScopes);
            }else{
                if(!secretEncoder.matches(clientSecret, found.get().getHashedSecret())){
                    throw new OAuth2AuthorizationException(new OAuth2Error(
                            OAuth2ErrorCodes.ACCESS_DENIED),
                            "client_id and/or client_secret are invalid");
                }
            }
            String subject = null;
            AppInfoDO app = found.get().getApp();
            if(app != null)
                subject = app.getAppId().toString();

            List<String>[] audAndScp = allowedScopes.getAudAndScp(scopes);
            JWSObject token = new JWSBuilder()
                    .expIn(Constants.TOKEN_EXPIRY_DURATION_SECONDS)
                    .sub(subject)
                    .aud(audAndScp[0])
                    .scp(audAndScp[1])
                    .build(signer);

            return OAuth2AccessTokenResponse
                    .withToken(token.serialize())
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .expiresIn(Constants.TOKEN_EXPIRY_DURATION_SECONDS)
                    .scopes(scopes.keySet())
                    .refreshToken(refreshService.issue(subject, audAndScp[0], audAndScp[1]))
                    .build();
        } catch (JOSEException e) {
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "Issue with JWT construction",
                    null
            ), e);
        }
    }

    private void guardAppUser(String userId, String appId){
        Optional<UserInfoDO> user = userInfoService.getDO(userId);
        if(user.isEmpty())
            throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN).build();
        List<String> allowedApps = user.get().getOrg().getApps()
                .stream()
                .map(app -> app.getAppId().toString())
                .toList();
        if(!allowedApps.contains(appId))
            throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN).build();
    }

    private String generateSecret(int len){
        byte[] secret = new byte[len];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(secret);
        return Base64.getEncoder().withoutPadding().encodeToString(secret);
    }
}
