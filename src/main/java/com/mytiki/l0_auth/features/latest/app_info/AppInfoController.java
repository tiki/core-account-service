/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.app_info;

import com.mytiki.l0_auth.security.OauthScopes;
import com.mytiki.l0_auth.utilities.Constants;
import com.mytiki.spring_rest_api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "")
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
            security = @SecurityRequirement(name = "oauth", scopes = "auth"))
    @RequestMapping(method = RequestMethod.GET, path = "/{appId}")
    public AppInfoAO get(JwtAuthenticationToken token, @PathVariable String appId) {
        if(OauthScopes.hasScope(token, "internal:read"))
            return service.get(appId);
        return service.getForUser(token.getName(), appId);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-app-create",
            summary = "Create App",
            description = "Create a new App",
            security = @SecurityRequirement(name = "oauth", scopes = "auth"))
    @RequestMapping(method = RequestMethod.POST)
    public AppInfoAO get(Principal principal, @RequestBody AppInfoAOReq body) {
        return service.create(body.getName(), principal.getName());
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-app-update",
            summary = "Update App",
            description = "Create a new App",
            security = @SecurityRequirement(name = "oauth", scopes = "auth"))
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
            security = @SecurityRequirement(name = "oauth", scopes = "auth"))
    @RequestMapping(method = RequestMethod.DELETE, path = "/{appId}")
    public void delete(Principal principal, @PathVariable String appId) {
        service.delete(principal.getName(), appId);
    }
}
