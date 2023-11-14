/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.api_key;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.user_info.UserInfoDO;
import com.mytiki.account.security.oauth.OauthScopes;
import com.mytiki.account.security.oauth.OauthSub;
import com.mytiki.account.security.oauth.OauthSubNamespace;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import com.mytiki.account.utilities.builder.JwtBuilder;
import com.mytiki.account.utilities.facade.readme.ReadmeF;
import com.mytiki.account.utilities.facade.readme.ReadmeReq;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

@XRayEnabled
public class ApiKeyService {
    private final ApiKeyRepository repository;
    private final JWSSigner signer;
    private final ReadmeF readme;

    public ApiKeyService(ApiKeyRepository repository, JWSSigner signer, ReadmeF readme) {
        this.repository = repository;
        this.signer = signer;
        this.readme = readme;
    }

    public Map<String, String> readme(ReadmeReq req, String signature){
        if(!readme.verify(req, signature))
            throw new ErrorBuilder(HttpStatus.FORBIDDEN).exception();
        Map<String, String> rsp = new HashMap<>();
        List<ApiKeyDO> keys = repository.findAllByUserEmail(req.getEmail());
        keys.forEach((key) -> rsp.put(key.getLabel(), key.getToken()));
        return rsp;
    }

    public void revoke(String token){
        repository.deleteByToken(token);
    }

    @Transactional
    public OAuth2AccessTokenResponse authorize(
            OauthSub sub,
            String clientSecret,
            OauthScopes scopes,
            Long expires){
        String[] split = sub.getId().split(":");
        String label = split.length < 2 ? "default" : split[1];
        Optional<ApiKeyDO> found = repository.findByTokenAndUserUserId(clientSecret, UUID.fromString(split[0]));
        if(found.isEmpty())
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT));
        UserInfoDO user = found.get().getUser();
        OauthSub subject = new OauthSub(OauthSubNamespace.USER, user.getUserId().toString());
        try {
            String token = new JwtBuilder()
                    .exp(expires)
                    .sub(subject)
                    .aud(scopes.getAud())
                    .scp(scopes.getScp())
                    .build()
                    .sign(signer)
                    .toToken();
            ApiKeyDO apiKey = new ApiKeyDO();
            apiKey.setUser(user);
            apiKey.setLabel(label);
            apiKey.setToken(token);
            apiKey.setCreated(ZonedDateTime.now());
            repository.save(apiKey);
            return OAuth2AccessTokenResponse
                    .withToken(token)
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .scopes(new HashSet<>(scopes.getScp()))
                    .expiresIn(expires * 1000)
                    .build();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}
