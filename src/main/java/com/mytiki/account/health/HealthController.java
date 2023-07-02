/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.health;

import com.mytiki.spring_rest_api.ApiConstants;
import com.mytiki.spring_rest_api.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = ApiConstants.HEALTH_ROUTE)
public class HealthController {

    @Operation(hidden = true)
    @RequestMapping(method = RequestMethod.GET)
    public ApiError get() {
        ApiError rsp = new ApiError();
        rsp.setMessage("OK");
        return rsp;
    }
}
