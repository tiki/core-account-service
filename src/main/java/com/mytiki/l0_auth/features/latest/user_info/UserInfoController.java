/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.user_info;

import com.mytiki.l0_auth.utilities.Constants;
import com.mytiki.spring_rest_api.ApiConstants;
import com.mytiki.spring_rest_api.ApiException;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
@Tag(name = "")
@RestController
@RequestMapping(value = UserInfoController.PATH_CONTROLLER)
public class UserInfoController {
    public static final String PATH_CONTROLLER = ApiConstants.API_LATEST_ROUTE + "user";

    private final UserInfoService service;

    public UserInfoController(UserInfoService service) {
        this.service = service;
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-user-post",
            summary = "Update a User",
            description = "Update the authorized user's profile",
            security = @SecurityRequirement(name = "oauth", scopes = "auth"))
    @RequestMapping(method = RequestMethod.POST, path =  "/{userId}")
    public UserInfoAO update(
            Principal principal,
            @PathVariable String userId,
            @RequestBody UserInfoAOUpdate body) {
        if(!principal.getName().equals(userId))
            throw new ApiException(HttpStatus.FORBIDDEN);
        if(body.getEmail() != null) {
            if (!EmailValidator.getInstance().isValid(body.getEmail()))
                throw new ApiExceptionBuilder(HttpStatus.BAD_REQUEST)
                        .message("Invalid email")
                        .build();
        }
        return service.update(userId, body);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-user-get",
            summary = "Get User",
            description = "Get the authorized user's profile",
            security = @SecurityRequirement(name = "oauth", scopes = "auth"))
    @RequestMapping(method = RequestMethod.GET)
    public UserInfoAO get(Principal principal) {
        if(principal == null || principal.getName() == null)
            throw new ApiException(HttpStatus.FORBIDDEN);
        return service.get(principal.getName());
    }

    @Operation(hidden = true)
    @Secured("SCOPE_internal:read")
    @RequestMapping(method = RequestMethod.GET, path =  "/{userId}")
    public UserInfoAO getById(@PathVariable String userId) {
        return service.get(userId);
    }
}
