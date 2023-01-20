/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.refresh;

import com.mytiki.l0_auth.utilities.Constants;
import com.mytiki.l0_auth.utilities.JWSBuilder;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RefreshService {
    private final RefreshRepository repository;
    private final JWSSigner jwtSigner;
    private final JwtDecoder jwtDecoder;

    public RefreshService(RefreshRepository repository, JWSSigner jwtSigner, JwtDecoder jwtDecoder) {
        this.repository = repository;
        this.jwtSigner = jwtSigner;
        this.jwtDecoder = jwtDecoder;
    }

    public String issue(String sub, List<String> aud) throws JOSEException {
        RefreshDO refreshDO = new RefreshDO();
        ZonedDateTime now = ZonedDateTime.now();
        refreshDO.setJti(UUID.randomUUID());
        refreshDO.setIssued(now);
        refreshDO.setExpires(now.plusSeconds(Constants.REFRESH_EXPIRY_DURATION_SECONDS));
        repository.save(refreshDO);
        return new JWSBuilder()
                .iat(refreshDO.getIssued())
                .exp(refreshDO.getExpires())
                .sub(sub)
                .aud(aud)
                .jti(refreshDO.getJti().toString())
                .build(jwtSigner)
                .serialize();
    }

    public OAuth2AccessTokenResponse authorize(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Optional<RefreshDO> found = repository.findByJti(UUID.fromString(jwt.getId()));
            if (found.isPresent()) {
                repository.delete(found.get());
                JWSObject newToken = new JWSBuilder()
                        .sub(jwt.getSubject())
                        .aud(jwt.getAudience())
                        .expIn(Constants.TOKEN_EXPIRY_DURATION_SECONDS)
                        .build(jwtSigner);
                return OAuth2AccessTokenResponse
                        .withToken(newToken.serialize())
                        .tokenType(OAuth2AccessToken.TokenType.BEARER)
                        .refreshToken(issue(jwt.getSubject(), jwt.getAudience()))
                        .expiresIn(Constants.TOKEN_EXPIRY_DURATION_SECONDS)
                        .build();
            } else
                throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT));
        } catch (JOSEException | JwtException e) {
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT), e);
        }
    }

    @Transactional
    public void revoke(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            repository.deleteByJti(UUID.fromString(jwt.getId()));
        } catch (JwtException e) {
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT), e);
        }
    }
}
