/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.api_key;

import com.mytiki.account.features.latest.app_info.AppInfoDO;
import com.mytiki.account.features.latest.app_info.AppInfoService;
import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.security.oauth.OauthInternal;
import com.mytiki.account.security.oauth.OauthScopes;
import com.mytiki.account.security.oauth.OauthSub;
import com.mytiki.account.security.oauth.OauthSubNamespace;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.builder.JwtBuilder;
import com.mytiki.account.utilities.facade.B64F;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ApiKeyService {
    private final ApiKeyRepository repository;
    private final AppInfoService appInfoService;
    private final RefreshService refreshService;
    private final JWSSigner signer;
    private final PasswordEncoder secretEncoder;
    private final OauthScopes allowedScopes;
    private final List<String> publicScopes;
    private final OauthInternal oauthInternal;

    public ApiKeyService(
            ApiKeyRepository repository,
            AppInfoService appInfoService,
            RefreshService refreshService,
            JWSSigner signer,
            OauthScopes allowedScopes,
            List<String> publicScopes,
            OauthInternal oauthInternal) {
        this.repository = repository;
        this.appInfoService = appInfoService;
        this.refreshService = refreshService;
        this.signer = signer;
        this.secretEncoder = new BCryptPasswordEncoder(12);
        this.allowedScopes = allowedScopes;
        this.publicScopes = publicScopes;
        this.oauthInternal = oauthInternal;
    }

    @Transactional
    public ApiKeyAOCreate create(String appId, boolean isPublic){
        String secret = null;
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

    public List<ApiKeyAO> getByAppId(String appId){
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
    public void revoke(String appId, String keyId){
        Optional<ApiKeyDO> found = repository.findByAppAppIdAndId(
                UUID.fromString(appId),
                UUID.fromString(keyId));
        found.ifPresent(repository::delete);
    }

    public OAuth2AccessTokenResponse authorize(String clientId, String clientSecret, String requestScopes){
        OauthSub subject = new OauthSub();
        OauthScopes scopes = allowedScopes.filter(requestScopes);
        OauthScopes internal = scopes.filter(oauthInternal.getScopes());

        if(!internal.getScopes().isEmpty()){
            String hashedSecret = oauthInternal.getKeys().get(clientId);
            if(!secretEncoder.matches(clientSecret, hashedSecret))
                throw new OAuth2AuthorizationException(new OAuth2Error(
                        OAuth2ErrorCodes.ACCESS_DENIED),
                        "client_id and/or client_secret are invalid");
            subject = new OauthSub(OauthSubNamespace.APP, clientId);
        }else {
            UUID clientUUID;
            try {
                 clientUUID = UUID.fromString(clientId);
            }catch (IllegalArgumentException ex){
                throw new OAuth2AuthorizationException(new OAuth2Error(
                        OAuth2ErrorCodes.ACCESS_DENIED,
                        "client_id and/or client_secret are invalid",
                        null
                ));
            }
            Optional<ApiKeyDO> found = repository.findById(clientUUID);
            if (found.isEmpty())
                throw new OAuth2AuthorizationException(new OAuth2Error(
                        OAuth2ErrorCodes.ACCESS_DENIED,
                        "client_id and/or client_secret are invalid",
                        null
                ));
            if (found.get().getHashedSecret() == null && clientSecret == null) {
                scopes = scopes.filter(publicScopes);
            } else {
                if (!secretEncoder.matches(clientSecret, found.get().getHashedSecret())) {
                    throw new OAuth2AuthorizationException(new OAuth2Error(
                            OAuth2ErrorCodes.ACCESS_DENIED),
                            "client_id and/or client_secret are invalid");
                }
            }
            AppInfoDO app = found.get().getApp();
            if (app != null)
                subject = new OauthSub(OauthSubNamespace.APP, app.getAppId().toString());
        }
        try{
            return new JwtBuilder()
                    .exp(Constants.TOKEN_EXPIRY_DURATION_SECONDS)
                    .sub(subject)
                    .aud(scopes.getAud())
                    .scp(scopes.getScp())
                    .refresh(refreshService.issue(subject, scopes.getAud(), scopes.getScp()))
                    .build()
                    .sign(signer)
                    .toResponse();
        } catch (JOSEException e) {
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "Issue with JWT construction",
                    null
            ), e);
        }
    }

    private String generateSecret(int len){
        byte[] secret = new byte[len];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(secret);
        return B64F.encode(secret);
    }
}
