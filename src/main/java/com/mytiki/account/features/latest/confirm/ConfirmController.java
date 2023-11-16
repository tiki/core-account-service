/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.confirm;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@XRayEnabled
@RestController
@RequestMapping(value = ConfirmController.ROUTE)
public class ConfirmController {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "confirm";
    private final ConfirmService service;

    public ConfirmController(ConfirmService service) {
        this.service = service;
    }

    @Operation(hidden = true)
    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(code = HttpStatus.OK)
    public void update(@RequestParam String token) {
        service.confirm(token);
    }
}
