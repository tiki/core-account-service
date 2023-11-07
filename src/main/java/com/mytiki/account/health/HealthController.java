/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.health;


import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.utilities.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = HealthController.ROUTE)
public class HealthController {
    public static final String ROUTE = "/health";

    @Operation(hidden = true)
    @RequestMapping(method = RequestMethod.GET)
    public ApiError get() {
        ApiError rsp = new ApiError();
        rsp.setMessage("OK");
        return rsp;
    }
}
