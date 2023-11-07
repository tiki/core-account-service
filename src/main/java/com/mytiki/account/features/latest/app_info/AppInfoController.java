/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.app_info;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@XRayEnabled
@Tag(name = "")
@RestController
@RequestMapping(value = AppInfoController.PATH_CONTROLLER)
public class AppInfoController {
    public static final String PATH_CONTROLLER = Constants.API_LATEST_ROUTE + "app";

    private final AppInfoService service;

    public AppInfoController(AppInfoService service) {
        this.service = service;
    }


    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-app-get",
            summary = "Get App",
            description = "Retrieve the details about an App",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @RequestMapping(method = RequestMethod.GET, path = "/{app-id}")
    @Secured({"SCOPE_account:admin", "SCOPE_account:internal:read"})
    public AppInfoAO get(
            JwtAuthenticationToken token,
            @PathVariable(name = "app-id") String appId) {
        service.guard(token, appId);
        return service.get(appId);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-app-create",
            summary = "Create App",
            description = "Create a new App",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.POST)
    public AppInfoAO create(
            JwtAuthenticationToken token,
            @RequestBody AppInfoAOReq body) {
        return service.create(body.getName(), token.getName());
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-app-update",
            summary = "Update App",
            description = "Update the details for an App",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.POST,  path = "/{app-id}")
    public AppInfoAO update(
            JwtAuthenticationToken token,
            @PathVariable(name = "app-id") String appId,
            @RequestBody AppInfoAOReq body) {
        service.guard(token, appId);
        return service.update(appId, body);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-app-delete",
            summary = "Delete App",
            description = "Permanently delete an App",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.DELETE, path = "/{app-id}")
    public void delete(
            JwtAuthenticationToken token,
            @PathVariable(name = "app-id") String appId) {
        service.guard(token, appId);
        service.delete(appId);
    }
}
