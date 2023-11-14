/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.api_key;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.facade.readme.ReadmeReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@XRayEnabled
@Tag(name = "")
@RestController
@RequestMapping(value = ApiKeyController.ROUTE)
public class ApiKeyController {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "api-key";

    private final ApiKeyService service;

    public ApiKeyController(ApiKeyService service) {
        this.service = service;
    }

    @Operation(hidden = true)
    @RequestMapping(method = RequestMethod.GET)
    public Map<String, String> get(
            @RequestHeader(name = "readme-signature") String signature,
            @RequestBody ReadmeReq body) {
        return service.readme(body, signature);
    }
}
