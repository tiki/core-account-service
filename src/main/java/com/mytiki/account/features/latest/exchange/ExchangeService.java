/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.exchange;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mytiki.account.features.latest.exchange.github.GithubClient;
import com.mytiki.account.features.latest.exchange.google.GoogleClient;
import com.mytiki.account.features.latest.exchange.shopify.ShopifyClient;
import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.features.latest.user_info.UserInfoDO;
import com.mytiki.account.features.latest.user_info.UserInfoService;
import com.mytiki.account.security.oauth.OauthScopes;
import com.mytiki.account.security.oauth.OauthSub;
import com.mytiki.account.security.oauth.OauthSubNamespace;
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

@XRayEnabled
public class ExchangeService {

    private final UserInfoService userInfoService;
    private final RefreshService refreshService;
    private final OauthScopes allowedScopes;
    private final JWSSigner signer;
    private final ReadmeF readme;
    private final GoogleClient google;
    private final GithubClient github;
    private final ShopifyClient shopify;

    public ExchangeService(
            UserInfoService userInfoService,
            RefreshService refreshService,
            JWSSigner signer,
            OauthScopes allowedScopes,
            ReadmeF readme,
            GoogleClient google,
            GithubClient github,
            ShopifyClient shopify) {
        this.userInfoService = userInfoService;
        this.refreshService = refreshService;
        this.signer = signer;
        this.allowedScopes = allowedScopes;
        this.readme = readme;
        this.google = google;
        this.github = github;
        this.shopify = shopify;
    }

    @Transactional
    public OAuth2AccessTokenResponse authorize(
            OauthScopes scopes, String clientId, String subjectToken, String subjectTokenType) {
        String email = validate(clientId, subjectToken, subjectTokenType);
        UserInfoDO userInfo = userInfoService.createIfNotExists(email);
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
                    .additional("readme_token", readme.sign(userInfo))
                    .toResponse();
        } catch (JOSEException | JsonProcessingException e) {
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "Issue with JWT construction",
                    null
            ), e);
        }
    }


    private String validate(String clientId, String subjectToken, String subjectTokenType) {
        return switch (subjectTokenType) {
            case "urn:mytiki:params:oauth:token-type:shopify" -> shopify.validate(clientId, subjectToken);
            case "urn:mytiki:params:oauth:token-type:google" -> google.validate(clientId, subjectToken);
            case "urn:mytiki:params:oauth:token-type:github" -> github.validate(clientId, subjectToken);
            default -> throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.ACCESS_DENIED),
                    "client_id and/or subject_token_type are invalid");
        };
    }
}
