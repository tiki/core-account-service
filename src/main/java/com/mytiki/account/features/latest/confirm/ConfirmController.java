/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.confirm;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.user_info.UserInfoAO;
import com.mytiki.account.features.latest.user_info.UserInfoAOUpdate;
import com.mytiki.account.features.latest.user_info.UserInfoService;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@XRayEnabled
@Tag(name = "")
@RestController
@RequestMapping(value = ConfirmController.PATH_CONTROLLER)
public class ConfirmController {
    public static final String PATH_CONTROLLER = Constants.API_LATEST_ROUTE + "confirm";
    private final ConfirmService service;

    public ConfirmController(ConfirmService service) {
        this.service = service;
    }

    @Operation(
            hidden = true,
            operationId = Constants.PROJECT_DASH_PATH +  "-confirm-get",
            summary = "Confirm a change request",
            description = "Process a change request that requires additional confirmation"
    )
    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(code = HttpStatus.OK)
    public void confirm(@RequestParam String token) {
        service.confirm(token);
    }
}
