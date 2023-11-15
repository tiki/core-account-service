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
@RequestMapping(value = OtpController.ROUTE)
public class OtpController {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "auth/otp";
    private final OtpService service;

    public OtpController(OtpService service) {
        this.service = service;
    }

    @Operation(hidden = true)
    @RequestMapping(method = RequestMethod.POST)
    public OtpAOStartRsp issue(@RequestBody OtpAOStartReq body) {
        return service.start(body);
    }
}
