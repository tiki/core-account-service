/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.refresh;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.builder.JwtBuilder;
import com.mytiki.account.utilities.facade.readme.ReadmeF;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@XRayEnabled
public class RefreshService {
    private final RefreshRepository repository;
    private final JWSSigner jwtSigner;
    private final JwtDecoder jwtDecoder;
    private final ReadmeF readme;
    private final ProfileService profileService;

    public RefreshService(
            RefreshRepository repository,
            JWSSigner jwtSigner,
            JwtDecoder jwtDecoder,
            ReadmeF readme,
            ProfileService profileService) {
        this.repository = repository;
        this.jwtSigner = jwtSigner;
        this.jwtDecoder = jwtDecoder;
        this.readme = readme;
        this.profileService = profileService;
    }

    public String issue(OauthSub sub, List<String> aud, List<String> scp) throws JOSEException {
        RefreshDO refreshDO = new RefreshDO();
        ZonedDateTime now = ZonedDateTime.now();
        refreshDO.setJti(UUID.randomUUID());
        refreshDO.setIssued(now);
        refreshDO.setExpires(now.plusSeconds(Constants.REFRESH_EXPIRY_DURATION_SECONDS));
        repository.save(refreshDO);
        return new JwtBuilder()
                .iat(refreshDO.getIssued())
                .exp(refreshDO.getExpires())
                .sub(sub)
                .aud(aud)
                .scp(scp)
                .jti(refreshDO.getJti().toString())
                .build()
                .sign(jwtSigner)
                .toToken();
    }

    public OAuth2AccessTokenResponse authorize(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Optional<RefreshDO> found = repository.findByJti(UUID.fromString(jwt.getId()));
            if (found.isPresent()) {
                repository.delete(found.get());
                JwtBuilder builder = new JwtBuilder()
                        .sub(jwt.getSubject())
                        .aud(jwt.getAudience())
                        .scp(jwt.getClaim("scp"))
                        .exp(Constants.TOKEN_EXPIRY_DURATION_SECONDS)
                        .refresh(issue(new OauthSub(jwt.getSubject()), jwt.getAudience(), jwt.getClaim("scp")))
                        .build()
                        .sign(jwtSigner);
                OauthSub sub = new OauthSub(jwt.getSubject());
                if(sub.isUser()){
                    ProfileDO profile = profileService.getDO(sub.getId()).orElseThrow();
                    return builder
                            .additional("readme_token", readme.sign(profile))
                            .toResponse();
                }else return builder.toResponse();
            } else
                throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT));
        } catch (JOSEException | JwtException | JsonProcessingException | NoSuchElementException e) {
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT), e);
        }
    }

    public void revoke(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            if(jwt.getId() != null) repository.deleteByJti(UUID.fromString(jwt.getId()));
        } catch (JwtException e) {
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN), e);
        }
    }
}
