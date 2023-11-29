/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.cleanroom;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@XRayEnabled
@Tag(name = "Data Purchaser")
@RestController
@RequestMapping(value = CleanroomController.ROUTE)
public class CleanroomController {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "cleanroom";

    private final CleanroomService service;

    public CleanroomController(CleanroomService service) {
        this.service = service;
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-cleanroom-get",
            summary = "Get Cleanroom",
            description = "Returns the configuration details for a data cleanroom",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @RequestMapping(method = RequestMethod.GET, path = "/{cleanroom-id}")
    @Secured("SCOPE_account:admin")
    public CleanroomAO get(JwtAuthenticationToken token, @PathVariable(name = "cleanroom-id") String cleanroomId) {
        return service.get(new OauthSub(token.getName()), cleanroomId);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-cleanroom-create",
            summary = "Create Cleanroom",
            description = "Creates a new data cleanroom, returning the configuration details",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.POST)
    public CleanroomAO create(JwtAuthenticationToken token, @RequestBody CleanroomAOReq body) {
        return service.create(body, new OauthSub(token.getName()));
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-cleanroom-update",
            summary = "Update a Cleanroom",
            description = "Updates a data cleanroom, replacing the settings with any non-null inputs",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.POST, path = "/{cleanroom-id}")
    public CleanroomAO update(
            JwtAuthenticationToken token,
            @RequestBody CleanroomAOReq body,
            @PathVariable(name = "cleanroom-id") String cleanroomId) {
        return service.update(new OauthSub(token.getName()), cleanroomId, body);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-cleanroom-delete",
            summary = "Delete Cleanroom",
            description = "Permanently delete a data cleanroom and all it's data",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.DELETE, path = "/{cleanroom-id}")
    public void delete(
            JwtAuthenticationToken token,
            @PathVariable(name = "cleanroom-id") String cleanroomId) {
        service.delete(new OauthSub(token.getName()), cleanroomId);
    }
}
