/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.otp;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
