/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.addr_reg;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.app_info.AppInfoService;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@XRayEnabled
@Tag(name = "")
@RestController
@RequestMapping(value = Constants.API_LATEST_ROUTE)
public class AddrRegController {
    public static final String ROUTE = "app/{app-id}/address";

    private final AddrRegService service;
    private final AppInfoService appInfo;

    public AddrRegController(AddrRegService service, AppInfoService appInfo) {
        this.service = service;
        this.appInfo = appInfo;
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-addr-reg-post",
            summary = "Register Address",
            description = "Register an end user address",
            security = @SecurityRequirement(name = "oauth", scopes = "account:app"))
    @Secured("SCOPE_account:app")
    @RequestMapping(method = RequestMethod.POST, path = ROUTE)
    public AddrRegAORsp post(
            JwtAuthenticationToken token,
            @RequestHeader(value = "X-Customer-Authorization", required = false) String custAuth,
            @PathVariable(name = "app-id") String appId,
            AddrRegAOReq body) {
        appInfo.guard(token, appId);
        return service.register(appId, body, custAuth);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-addr-reg-get",
            summary = "Get Addresses",
            description = "Retrieve and filter registered addresses",
            security = @SecurityRequirement(name = "oauth", scopes = "account:app"))
    @Secured({"SCOPE_account:app", "SCOPE_account:internal:read"})
    @RequestMapping(method = RequestMethod.GET, path = ROUTE)
    public List<AddrRegAORsp> getAll(
            JwtAuthenticationToken token,
            @PathVariable(name = "app-id") String appId,
            @RequestParam(name = "address", required = false) String address,
            @RequestParam(name = "id", required = false) String id) {
        appInfo.guard(token, appId);
        if(address != null) return List.of(service.get(appId, address));
        else if(id != null) return service.getAll(appId, id);
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
            @PathVariable(name = "app-id") String appId,
            @RequestParam(name = "address", required = false) String address,
            @RequestParam(name = "id", required = false) String id) {
        appInfo.guard(token, appId);
        if(address != null) service.delete(appId, address);
        else if(id != null) service.deleteAll(appId, id);
    }

    @Operation(hidden = true)
    @RequestMapping(
            method = RequestMethod.POST,
            path = Constants.AUTH_TOKEN_PATH,
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            params = {"address", "signature"})
    public OAuth2AccessTokenResponse grant(
            @RequestParam(name = "grant_type") AuthorizationGrantType grantType,
            @RequestParam(required = false) String scope,
            @RequestParam(name = "app-id", required = false) String appId,
            @RequestParam(name = "address", required = false) String address,
            @RequestParam(name = "signature", required = false) String signature) {
        if (!grantType.equals(new AuthorizationGrantType("urn:mytiki:params:oauth:grant-type:rsa")))
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.UNSUPPORTED_GRANT_TYPE));
        return service.authorize(scope, appId, address, signature);
    }
}
