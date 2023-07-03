/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.otp;

import com.mytiki.account.utilities.Constants;
import com.mytiki.spring_rest_api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "")
@RestController
@RequestMapping(value = ApiConstants.API_LATEST_ROUTE)
public class OtpController {
    public static final String PATH_ISSUE = "otp/start";

    private final OtpService service;

    public OtpController(OtpService service) {
        this.service = service;
    }

    @Operation(
            operationId = Constants.PROJECT_DASH_PATH +  "-otp-start-post",
            summary = "Request OTP",
            description = "Start a new passwordless authorization flow")
    @RequestMapping(method = RequestMethod.POST, path = PATH_ISSUE)
    public OtpAOStartRsp issue(@RequestBody OtpAOStartReq body) {
        return service.start(body);
    }

    @Operation(
            operationId = Constants.PROJECT_DASH_PATH +  "-oauth-token-post",
            summary = "Token Grant",
            description = "Issue authorization token. Use password grant for OTP flow.")
    @RequestMapping(
            method = RequestMethod.POST,
            path = Constants.OAUTH_TOKEN_PATH,
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            params = {"username", "password"})
    public OAuth2AccessTokenResponse grant(
            @Parameter(
                    schema = @Schema(type = "string"),
                    description = "(password, refresh_token, client_credentials)") //, urn:ietf:params:oauth:grant-type:jwt-bearer
            @RequestParam(name = "grant_type") AuthorizationGrantType grantType,
            @RequestParam(required = false) String scope,
            @RequestParam(name = "username") String deviceId,
            @RequestParam(name = "password") String code) {
        if (!grantType.equals(AuthorizationGrantType.PASSWORD))
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.UNSUPPORTED_GRANT_TYPE));
        return service.authorize(deviceId, code, scope);
    }
}