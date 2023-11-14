/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.security.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.utilities.builder.JwtBuilder;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import java.util.List;
import java.util.Map;

public class OauthInternal {
    private final RefreshService refreshService;
    private final PasswordEncoder secretEncoder;
    private final JWSSigner signer;
    private final OauthInternalCreds creds;

    public OauthInternal(
            RefreshService refreshService,
            JWSSigner signer,
            OauthInternalCreds creds) {
        this.refreshService = refreshService;
        this.secretEncoder = new BCryptPasswordEncoder(12);
        this.signer = signer;
        this.creds = creds;
    }

    public OAuth2AccessTokenResponse authorize(
            OauthSub sub,
            String clientSecret,
            OauthScopes scopes,
            Long expires) {
        String hashedSecret = creds.getKeys().get(sub.getId());
        if (!secretEncoder.matches(clientSecret, hashedSecret))
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT));
        try {
            return new JwtBuilder()
                    .exp(expires)
                    .sub(sub)
                    .aud(scopes.getAud())
                    .scp(scopes.getScp())
                    .refresh(refreshService.issue(sub, scopes.getAud(), scopes.getScp()))
                    .build()
                    .sign(signer)
                    .toResponse();
        } catch (JOSEException e) {
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR), "Issue with JWT construction", e);
        }
    }
}
