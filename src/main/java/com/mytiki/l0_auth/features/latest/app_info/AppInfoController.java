/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.app_info;

import com.mytiki.l0_auth.utilities.Constants;
import com.mytiki.spring_rest_api.ApiConstants;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "APP")
@RestController
@RequestMapping(value = AppInfoController.PATH_CONTROLLER)
public class AppInfoController {
    public static final String PATH_CONTROLLER = ApiConstants.API_LATEST_ROUTE + "app";

    private final AppInfoService service;

    public AppInfoController(AppInfoService service) {
        this.service = service;
    }


    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-app-get",
            summary = "Get App",
            description = "Get an app's profile",
            security = @SecurityRequirement(name = "jwt"))
    @RequestMapping(method = RequestMethod.GET, path = "/{appId}")
    public AppInfoAO get(Principal principal, @PathVariable String appId) {
        AppInfoAO rsp = service.get(appId);
        if(rsp.getUsers().contains(principal.getName()))
            return rsp;
        else
            throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN).build();
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-app-create",
            summary = "Create App",
            description = "Create a new App",
            security = @SecurityRequirement(name = "jwt"))
    @RequestMapping(method = RequestMethod.POST)
    public AppInfoAO get(Principal principal, @RequestBody AppInfoAOReq body) {
        return service.create(body.getName(), principal.getName());
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-app-update",
            summary = "Update App",
            description = "Create a new App",
            security = @SecurityRequirement(name = "jwt"))
    @RequestMapping(method = RequestMethod.POST,  path = "/{appId}")
    public AppInfoAO get(
            Principal principal,
            @PathVariable String appId,
            @RequestBody AppInfoAOReq body) {
        return service.update(principal.getName(), appId, body);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-app-delete",
            summary = "Delete App",
            description = "Delete an App",
            security = @SecurityRequirement(name = "jwt"))
    @RequestMapping(method = RequestMethod.DELETE, path = "/{appId}")
    public void delete(Principal principal, @PathVariable String appId) {
        service.delete(principal.getName(), appId);
    }
}
