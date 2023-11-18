/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.auth_code;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.auth_code.github.GithubClient;
import com.mytiki.account.features.latest.auth_code.google.GoogleClient;
import com.mytiki.account.features.latest.oauth.OauthScopes;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.oauth.OauthSubNamespace;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.features.latest.readme.ReadmeService;
import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.builder.JwtBuilder;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

@XRayEnabled
public class AuthCodeService {

    private final ProfileService profileService;
    private final RefreshService refreshService;
    private final JWSSigner signer;
    private final ReadmeService readme;
    private final GoogleClient google;
    private final GithubClient github;

    public AuthCodeService(
            ProfileService profileService,
            RefreshService refreshService,
            JWSSigner signer,
            ReadmeService readme,
            GoogleClient google,
            GithubClient github) {
        this.profileService = profileService;
        this.refreshService = refreshService;
        this.signer = signer;
        this.readme = readme;
        this.google = google;
        this.github = github;
    }

    @Transactional
    public OAuth2AccessTokenResponse authorize(OauthScopes scopes, String clientId, String code) {
        String email = exchange(clientId, code);
        ProfileDO userInfo = profileService.createIfNotExists(email);
        OauthSub subject = new OauthSub(OauthSubNamespace.USER, userInfo.getUserId().toString());
        try {
            return new JwtBuilder()
                    .exp(Constants.TOKEN_EXPIRY_DURATION_SECONDS)
                    .sub(subject)
                    .aud(scopes.getAud())
                    .scp(scopes.getScp())
                    .refresh(refreshService.issue(subject, scopes.getAud(), scopes.getScp()))
                    .build()
                    .sign(signer)
                    .additional("readme_token", readme.authorize(userInfo))
                    .toResponse();
        } catch (JOSEException e) {
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "Issue with JWT construction",
                    null
            ), e);
        }
    }


    private String exchange(String clientId, String code) {
        if(clientId.equals(google.clientId())) return google.exchange(code);
        else if(clientId.equals(github.clientId())) return github.exchange(code);
        else throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.ACCESS_DENIED),
                    "client_id and/or subject_token_type are invalid");
    }
}
