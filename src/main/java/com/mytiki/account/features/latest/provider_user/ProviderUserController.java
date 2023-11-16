/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.provider_user;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.provider.ProviderService;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@XRayEnabled
@Tag(name = "")
@RestController
@RequestMapping(value = Constants.API_LATEST_ROUTE)
public class ProviderUserController {
    public static final String ROUTE = "provider/{provider-id}/user";

    private final ProviderUserService service;

    public ProviderUserController(ProviderUserService service) {
        this.service = service;
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-addr-reg-post",
            summary = "Register Address",
            description = "Register an end user address",
            security = @SecurityRequirement(name = "oauth", scopes = "account:provider"))
    @Secured("SCOPE_account:provider")
    @RequestMapping(method = RequestMethod.POST, path = ROUTE)
    public ProviderUserAORsp post(
            JwtAuthenticationToken token,
            @PathVariable(name = "provider-id") String providerId,
            ProviderUserAOReq body) {
        service.guard(token, providerId);
        return service.register(providerId, body);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-addr-reg-get",
            summary = "Get Addresses",
            description = "Retrieve and filter registered addresses",
            security = @SecurityRequirement(name = "oauth", scopes = "account:provider"))
    @Secured({"SCOPE_account:provider", "SCOPE_account:internal:read"})
    @RequestMapping(method = RequestMethod.GET, path = ROUTE)
    public List<ProviderUserAORsp> getAll(
            JwtAuthenticationToken token,
            @PathVariable(name = "provider-id") String providerId,
            @RequestParam(name = "address", required = false) String address,
            @RequestParam(name = "id", required = false) String id) {
        service.guard(token, providerId);
        if(address != null) return List.of(service.get(providerId, address));
        else if(id != null) return service.getAll(providerId, id);
        else return List.of();
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-addr-reg-delete",
            summary = "Delete Addresses",
            description = "Delete registered addresses",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.DELETE, path = ROUTE)
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteAll(
            JwtAuthenticationToken token,
            @PathVariable(name = "provider-id") String providerId,
            @RequestParam(name = "address", required = false) String address,
            @RequestParam(name = "id", required = false) String id) {
        service.guard(token, providerId);
        if(address != null) service.delete(providerId, address);
        else if(id != null) service.deleteAll(providerId, id);
    }
}
