/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.api_key;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.app_info.AppInfoService;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "App")
@RestController
@RequestMapping(value = Constants.API_LATEST_ROUTE)
public class ApiKeyController {
    public static final String ROUTE = "app/{app-id}/key";

    private final ApiKeyService service;
    private final AppInfoService appInfo;

    public ApiKeyController(ApiKeyService service, AppInfoService appInfo) {
        this.service = service;
        this.appInfo = appInfo;
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-api-keys-get",
            summary = "Get API Keys",
            description = "Retrieve the API Keys for an App",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.GET, path = ROUTE)
    public List<ApiKeyAO> get(JwtAuthenticationToken token, @PathVariable(name = "app-id") String appId) {
        appInfo.guard(token, appId);
        return service.getByAppId(appId);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-api-keys-create",
            summary = "Create API Key",
            description = "Create a new API Key for an App",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.POST, path = ROUTE)
    public ApiKeyAOCreate createAppKey(
            JwtAuthenticationToken token,
            @PathVariable(name = "app-id") String appId,
            @RequestParam(required = false) boolean isPublic) {
        appInfo.guard(token, appId);
        return service.create(appId, isPublic);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-api-keys-delete",
            summary = "Delete Key",
            description = "Permanently revoke an API Key",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.DELETE, path = ROUTE)
    public void revoke(
            JwtAuthenticationToken token,
            @PathVariable(name = "app-id") String appId,
            @RequestParam(name = "key-id") String keyId) {
        appInfo.guard(token, appId);
        service.revoke(appId, keyId);
    }

    @Operation(hidden = true)
    @RequestMapping(
            method = RequestMethod.POST,
            path = Constants.AUTH_TOKEN_PATH,
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            params = {"client_id", "client_secret"})
    public OAuth2AccessTokenResponse grant(
            @RequestParam(name = "grant_type") AuthorizationGrantType grantType,
            @RequestParam(required = false) String scope,
            @RequestParam(name = "client_id", required = false) String clientId,
            @RequestParam(name = "client_secret", required = false) String clientSecret) {
        if(clientSecret.isEmpty()) clientSecret = null;
        if (!grantType.equals(AuthorizationGrantType.CLIENT_CREDENTIALS))
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.UNSUPPORTED_GRANT_TYPE));
        return service.authorize(clientId, clientSecret, scope);
    }
}
