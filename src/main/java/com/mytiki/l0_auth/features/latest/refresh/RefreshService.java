/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.refresh;

import com.mytiki.l0_auth.utilities.Constants;
import com.nimbusds.jose.*;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.sql.Date;
import java.time.Instant;
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

    public String token(String sub, List<String> aud) throws JOSEException {
        RefreshDO refreshDO = new RefreshDO();
        ZonedDateTime now = ZonedDateTime.now();

        refreshDO.setJti(UUID.randomUUID());
        refreshDO.setIssued(now);
        refreshDO.setExpires(now.plusSeconds(Constants.REFRESH_EXPIRY_DURATION_SECONDS));
        repository.save(refreshDO);

        JWSObject jwsObject = new JWSObject(
                new JWSHeader
                        .Builder(JWSAlgorithm.ES256)
                        .type(JOSEObjectType.JWT)
                        .build(),
                new Payload(
                        new JWTClaimsSet.Builder()
                                .issuer(Constants.MODULE_DOT_PATH)
                                .issueTime(Date.from(refreshDO.getIssued().toInstant()))
                                .expirationTime(Date.from(refreshDO.getExpires().toInstant()))
                                .subject(sub)
                                .audience(aud)
                                .jwtID(refreshDO.getJti().toString())
                                .build()
                                .toJSONObject()
                ));

        jwsObject.sign(jwtSigner);
        return jwsObject.serialize();
    }

    public OAuth2AccessTokenResponse authorize(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Optional<RefreshDO> found = repository.findByJti(UUID.fromString(jwt.getId()));
            if (found.isPresent()) {
                repository.delete(found.get());
                Instant iat = Instant.now();
                //TODO this should be in OtpService
                JWSObject accessToken = new JWSObject(
                        new JWSHeader
                                .Builder(JWSAlgorithm.ES256)
                                .type(JOSEObjectType.JWT)
                                .build(),
                        new Payload(
                                new JWTClaimsSet.Builder()
                                        .issuer(Constants.MODULE_DOT_PATH)
                                        .issueTime(Date.from(iat))
                                        .subject(jwt.getSubject())
                                        .audience(jwt.getAudience())
                                        .expirationTime(Date.from(iat.plusSeconds(Constants.TOKEN_EXPIRY_DURATION_SECONDS)))
                                        .build()
                                        .toJSONObject()
                        ));
                accessToken.sign(jwtSigner);
                return OAuth2AccessTokenResponse
                        .withToken(accessToken.serialize())
                        .tokenType(OAuth2AccessToken.TokenType.BEARER)
                        .refreshToken(token(jwt.getSubject(), jwt.getAudience()))
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
