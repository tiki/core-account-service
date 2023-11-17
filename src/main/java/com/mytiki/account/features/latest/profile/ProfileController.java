/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.profile;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@XRayEnabled
@Tag(name = "Profile")
@RestController
@RequestMapping(value = ProfileController.ROUTE)
public class ProfileController {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "profile";
    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-profile-post",
            summary = "Update Profile",
            description = "Update your profile information — see request body for modification options",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void update(JwtAuthenticationToken token, @RequestBody ProfileAOUpdate body) {
        OauthSub sub = new OauthSub(token.getName());
        if(!sub.isUser()) throw new ErrorBuilder(HttpStatus.FORBIDDEN).message("Request requires a user token").exception();
        service.update(sub.getId(), body);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-profile-get",
            summary = "Get Profile",
            description = "Returns your profile information",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.GET)
    public ProfileAO get(JwtAuthenticationToken token) {
        OauthSub sub = new OauthSub(token.getName());
        if(!sub.isUser()) throw new ErrorBuilder(HttpStatus.FORBIDDEN).message("Request requires a user token").exception();
        return service.get(sub.getId());
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-profile-delete",
            summary = "Delete Account",
            description = "Permanently delete your account — requires email confirmation",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void delete(JwtAuthenticationToken token) {
        OauthSub sub = new OauthSub(token.getName());
        if(!sub.isUser()) throw new ErrorBuilder(HttpStatus.FORBIDDEN).message("Request requires a user token").exception();
        service.delete(sub.getId());
    }

    @Operation(hidden = true)
    @Secured("SCOPE_account:internal:read")
    @RequestMapping(method = RequestMethod.GET, path =  "/{userId}")
    public ProfileAO getById(@PathVariable String userId) {
        return service.get(userId);
    }
}
