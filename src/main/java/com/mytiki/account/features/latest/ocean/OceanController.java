/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@XRayEnabled
@Tag(name = "Profile")
@RestController
@RequestMapping(value = OceanController.ROUTE)
public class OceanController {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "ocean";

    private final OceanService service;

    public OceanController(OceanService service) {
        this.service = service;
    }

    @Operation(hidden = true)
    @Secured("SCOPE_account:internal")
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.OK)
    public void update(@RequestBody OceanAO body) {
        service.update(body);
    }
}
