/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.provider;

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
@RequestMapping(value = ProviderController.ROUTE)
public class ProviderController {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "provider";

    private final ProviderService service;

    public ProviderController(ProviderService service) {
        this.service = service;
    }


    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-app-get",
            summary = "Get App",
            description = "Retrieve the details about an App",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @RequestMapping(method = RequestMethod.GET, path = "/{provider-id}")
    @Secured({"SCOPE_account:admin", "SCOPE_account:internal:read"})
    public ProviderAO get(
            JwtAuthenticationToken token,
            @PathVariable(name = "provider-id") String providerId) {
        service.guard(token, providerId);
        return service.get(providerId);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-app-create",
            summary = "Create App",
            description = "Create a new App",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.POST)
    public ProviderAO create(
            JwtAuthenticationToken token,
            @RequestBody ProviderAOReq body) {
        return service.create(body.getName(), token.getName());
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-app-update",
            summary = "Update App",
            description = "Update the details for an App",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.POST,  path = "/{provider-id}")
    public ProviderAO update(
            JwtAuthenticationToken token,
            @PathVariable(name = "provider-id") String providerId,
            @RequestBody ProviderAOReq body) {
        service.guard(token, providerId);
        return service.update(providerId, body);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-app-delete",
            summary = "Delete App",
            description = "Permanently delete an App",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.DELETE, path = "/{provider-id}")
    public void delete(
            JwtAuthenticationToken token,
            @PathVariable(name = "provider-id") String providerId) {
        service.guard(token, providerId);
        service.delete(providerId);
    }
}
