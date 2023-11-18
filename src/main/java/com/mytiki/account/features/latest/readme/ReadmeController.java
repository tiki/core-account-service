/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.readme;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@XRayEnabled
@Tag(name = "")
@RestController
@RequestMapping(value = ReadmeController.ROUTE)
public class ReadmeController {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "readme";
    private final ReadmeService service;

    public ReadmeController(ReadmeService service) {
        this.service = service;
    }

    @Operation(hidden = true)
    @RequestMapping(method = RequestMethod.POST)
    public ReadmeAORsp webhook(
            @RequestHeader(name = "readme-signature") String signature,
            @RequestBody ReadmeAOReq body) {
        return service.personalize(body, signature);
    }
}
