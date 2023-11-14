/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.otp;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.utilities.Constants;
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

@XRayEnabled
@Tag(name = "")
@RestController
@RequestMapping(value = Constants.API_LATEST_ROUTE)
public class OtpController {

    private final OtpService service;

    public OtpController(OtpService service) {
        this.service = service;
    }

    @Operation(
            operationId = Constants.PROJECT_DASH_PATH +  "-otp-start-post",
            summary = "Request OTP",
            description = "Start a one-time password (email) authorization flow")
    @RequestMapping(method = RequestMethod.POST, path = "auth/otp")
    public OtpAOStartRsp issue(@RequestBody OtpAOStartReq body) {
        return service.start(body);
    }
}
