/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.refresh;

import com.mytiki.account.utilities.Constants;
import com.mytiki.spring_rest_api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

@Tag(name = "")
@RestController
@RequestMapping(value = ApiConstants.API_LATEST_ROUTE)
public class RefreshController {

    private final RefreshService service;

    public RefreshController(RefreshService service) {
        this.service = service;
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-oauth-revoke-post",
            summary = "Revoke Token",
            description = "Revoke a refresh token.",
            security = @SecurityRequirement(name = "oauth", scopes = "auth"))
    @ApiResponse(responseCode = "200")
    @RequestMapping(
            method = RequestMethod.POST,
            path = Constants.OAUTH_REVOKE_PATH,
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public void revoke(@RequestParam String token) {
        service.revoke(token);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            path = Constants.OAUTH_TOKEN_PATH,
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            params = {"refresh_token"})
    public OAuth2AccessTokenResponse tokenGrantRefresh(
            @RequestParam(name = "grant_type") AuthorizationGrantType grantType,
            @RequestParam(name = "refresh_token") String refreshToken) {
        if (!grantType.equals(AuthorizationGrantType.REFRESH_TOKEN))
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.UNSUPPORTED_GRANT_TYPE));
        return service.authorize(refreshToken);
    }
}
