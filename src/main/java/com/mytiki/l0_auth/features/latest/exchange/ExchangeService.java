/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.exchange;

import com.mytiki.l0_auth.features.latest.exchange.shopify.ShopifyClient;
import com.mytiki.l0_auth.features.latest.refresh.RefreshService;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoAO;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoService;
import com.mytiki.l0_auth.security.JWSBuilder;
import com.mytiki.l0_auth.security.OauthScope;
import com.mytiki.l0_auth.security.OauthScopes;
import com.mytiki.l0_auth.utilities.Constants;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import java.util.List;
import java.util.Map;

public class ExchangeService {

    private final UserInfoService userInfoService;
    private final RefreshService refreshService;
    private final OauthScopes allowedScopes;
    private final JWSSigner signer;

    public ExchangeService(
            UserInfoService userInfoService,
            RefreshService refreshService,
            JWSSigner signer,
            OauthScopes allowedScopes) {
        this.userInfoService = userInfoService;
        this.refreshService = refreshService;
        this.signer = signer;
        this.allowedScopes = allowedScopes;
    }

    public OAuth2AccessTokenResponse authorize(
            String requestedScope, String clientId, String subjectToken, String subjectTokenType) {
        String email = validate(clientId, subjectToken, subjectTokenType);
        UserInfoAO userInfo = userInfoService.createIfNotExists(email);
        String subject = userInfo.getUserId();
        Map<String, OauthScope> scopes = allowedScopes.parse(requestedScope);
        List<String>[] audAndScp = allowedScopes.getAudAndScp(scopes);
        try {
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


    private String validate(String clientId, String subjectToken, String subjectTokenType) {
        switch (subjectTokenType) {
            case "urn:mytiki:params:oauth:token-type:shopify": {
                ShopifyClient shopify = new ShopifyClient();
                return shopify.validate(clientId, subjectToken);
            }
            default: {
                throw new OAuth2AuthorizationException(new OAuth2Error(
                        OAuth2ErrorCodes.ACCESS_DENIED),
                        "client_id and/or subject_token_type are invalid");
            }
        }
    }
}
