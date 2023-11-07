/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.exchange;

import com.mytiki.account.features.latest.exchange.shopify.ShopifyClient;
import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.features.latest.user_info.UserInfoAO;
import com.mytiki.account.features.latest.user_info.UserInfoService;
import com.mytiki.account.security.oauth.OauthScopes;
import com.mytiki.account.security.oauth.OauthSub;
import com.mytiki.account.security.oauth.OauthSubNamespace;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.builder.JwtBuilder;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

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
        OauthSub subject = new OauthSub(OauthSubNamespace.USER, userInfo.getUserId());
        OauthScopes scopes = allowedScopes.filter(requestedScope);
        try {
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
