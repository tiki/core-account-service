/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.profile;

import com.amazonaws.xray.spring.aop.XRayEnabled;
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
@RequestMapping(value = ProfileController.ROUTE)
public class ProfileController {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "profile";
    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-user-post",
            summary = "Update User",
            description = "Update the authorized User's profile",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void update(JwtAuthenticationToken token, @RequestBody ProfileAOUpdate body) {
        service.update(token.getName(), body);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-user-get",
            summary = "Get User",
            description = "Get the authorized user's profile",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.GET)
    public ProfileAO get(JwtAuthenticationToken token) {
        return service.get(token.getName());
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-user-delete",
            summary = "Delete User",
            description = "Delete the authorized user",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void delete(JwtAuthenticationToken token) {
        service.get(token.getName());
    }

    @Operation(hidden = true)
    @Secured("SCOPE_account:internal:read")
    @RequestMapping(method = RequestMethod.GET, path =  "/{userId}")
    public ProfileAO getById(@PathVariable String userId) {
        return service.get(userId);
    }
}
