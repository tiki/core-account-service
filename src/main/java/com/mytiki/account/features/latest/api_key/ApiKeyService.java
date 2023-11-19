/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.api_key;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.oauth.OauthScopes;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.oauth.OauthSubNamespace;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.utilities.builder.JwtBuilder;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@XRayEnabled
public class ApiKeyService {
    private final ApiKeyRepository repository;
    private final JWSSigner signer;
    private final JwtDecoder decoder;

    public ApiKeyService(ApiKeyRepository repository, JWSSigner signer, JwtDecoder decoder) {
        this.repository = repository;
        this.signer = signer;
        this.decoder = decoder;
    }

    public List<ApiKeyDO> getByEmail(String email) {
        return repository.findAllByProfileEmail(email);
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
        Optional<ApiKeyDO> found = repository.findByTokenAndProfileUserId(clientSecret, UUID.fromString(split[0]));
        if(found.isEmpty())
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT));
        ProfileDO user = found.get().getProfile();
        List<String> tokenScopes = decoder.decode(found.get().getToken()).getClaim("scp");
        if(!tokenScopes.contains("account:admin"))
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.INSUFFICIENT_SCOPE));
        try{
            ApiKeyDO key = create(user, label, scopes, expires);
            return OAuth2AccessTokenResponse
                    .withToken(key.getToken())
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .scopes(new HashSet<>(scopes.getScp()))
                    .expiresIn(expires * 1000)
                    .build();
        } catch (JOSEException e) {
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "Issue with JWT construction",
                    null
            ), e);
        }
    }

    public ApiKeyDO create(ProfileDO user, String label, OauthScopes scopes, Long expires) throws JOSEException {
        OauthSub subject = new OauthSub(OauthSubNamespace.USER, user.getUserId().toString());
        String token = new JwtBuilder()
                .exp(expires)
                .sub(subject)
                .aud(scopes.getAud())
                .scp(scopes.getScp())
                .build()
                .sign(signer)
                .toToken();
        ApiKeyDO apiKey = new ApiKeyDO();
        apiKey.setProfile(user);
        apiKey.setLabel(label);
        apiKey.setToken(token);
        apiKey.setCreated(ZonedDateTime.now());
        return repository.save(apiKey);
    }
}
