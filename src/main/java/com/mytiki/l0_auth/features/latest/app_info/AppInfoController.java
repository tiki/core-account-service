/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.app_info;

import com.mytiki.l0_auth.features.latest.user_info.UserInfoController;
import com.mytiki.l0_auth.utilities.Constants;
import com.mytiki.spring_rest_api.ApiConstants;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Tag(name = "APP")
@RestController
@RequestMapping(value = UserInfoController.PATH_CONTROLLER)
public class AppInfoController {
    public static final String PATH_CONTROLLER = ApiConstants.API_LATEST_ROUTE + "appinfo";

    private final AppInfoService service;

    public AppInfoController(AppInfoService service) {
        this.service = service;
    }


    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-appinfo-get",
            summary = "Get an App",
            description = "Get an app's profile",
            security = @SecurityRequirement(name = "jwt"))
    @RequestMapping(method = RequestMethod.GET, path = "/{appId}")
    public AppInfoAO get(Principal principal, @RequestParam String appId) {
        AppInfoAO rsp = service.get(appId);
        if(rsp.getUsers().contains(principal.getName()))
            return rsp;
        else
            throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN).build();
    }
}
