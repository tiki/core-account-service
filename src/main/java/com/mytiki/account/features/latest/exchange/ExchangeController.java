/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.exchange;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@XRayEnabled
@Tag(name = "")
@RestController
@RequestMapping(value = Constants.API_LATEST_ROUTE)
public class ExchangeController {

    private final ExchangeService service;

    public ExchangeController(ExchangeService service) {
        this.service = service;
    }

    @Operation(hidden = true)
    @RequestMapping(
            method = RequestMethod.POST,
            path = Constants.AUTH_TOKEN_PATH,
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            params = {"subject_token", "subject_token_type"})
    public OAuth2AccessTokenResponse grant(
            @RequestParam(name = "grant_type") AuthorizationGrantType grantType,
            @RequestParam(required = false) String scope,
            @RequestParam(name = "client_id", required = false) String clientId,
            @RequestParam(name = "subject_token", required = false) String subjectToken,
            @RequestParam(name = "subject_token_type", required = false) String subjectTokenType) {
        if (!grantType.equals(new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:token-exchange")))
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.UNSUPPORTED_GRANT_TYPE));
        return service.authorize(scope, clientId, subjectToken, subjectTokenType);
    }
}
