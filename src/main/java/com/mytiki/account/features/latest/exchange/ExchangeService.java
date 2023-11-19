/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.exchange;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.exchange.shopify.ShopifyClient;
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
public class ExchangeService {

    private final ProfileService profileService;
    private final RefreshService refreshService;
    private final JWSSigner signer;
    private final ReadmeService readme;
    private final ShopifyClient shopify;

    public ExchangeService(
            ProfileService profileService,
            RefreshService refreshService,
            JWSSigner signer,
            ReadmeService readme,
            ShopifyClient shopify) {
        this.profileService = profileService;
        this.refreshService = refreshService;
        this.signer = signer;
        this.readme = readme;
        this.shopify = shopify;
    }

    @Transactional
    public OAuth2AccessTokenResponse authorize(
            OauthScopes scopes, String clientId, String subjectToken, String subjectTokenType) {
        String email = validate(clientId, subjectToken, subjectTokenType);
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


    private String validate(String clientId, String subjectToken, String subjectTokenType) {
        return switch (subjectTokenType) {
            case "urn:mytiki:params:oauth:token-type:shopify" -> shopify.validate(clientId, subjectToken);
            default -> throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.ACCESS_DENIED),
                    "client_id and/or subject_token_type are invalid");
        };
    }
}
