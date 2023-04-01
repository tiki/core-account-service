/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.api_key;

import com.mytiki.l0_auth.utilities.Constants;
import com.mytiki.spring_rest_api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "")
@RestController
@RequestMapping(value = ApiConstants.API_LATEST_ROUTE)
public class ApiKeyController {
    public static final String PATH_KEY = "key/{keyId}";
    public static final String PATH_APP_KEY = "app/{appId}/key";

    private final ApiKeyService service;

    public ApiKeyController(ApiKeyService service) {
        this.service = service;
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-api-keys-get",
            summary = "Get App Keys",
            description = "Get the API Keys for the given App (appId)",
            security = @SecurityRequirement(name = "oauth", scopes = "auth"))
    @RequestMapping(method = RequestMethod.GET, path = PATH_APP_KEY)
    public List<ApiKeyAO> getAppKeys(Principal principal, @PathVariable String appId) {
        return service.getByAppId(principal.getName(), appId);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-api-keys-create",
            summary = "Create App Key",
            description = "Create a new API Key for the given App (appId)",
            security = @SecurityRequirement(name = "oauth", scopes = "auth"))
    @RequestMapping(method = RequestMethod.POST, path = PATH_APP_KEY)
    public ApiKeyAOCreate createAppKey(
            Principal principal,
            @PathVariable String appId,
            @RequestParam(required = false) boolean isPublic) {
        return service.create(principal.getName(), appId, isPublic);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-api-keys-delete",
            summary = "Delete Key",
            description = "Revoke (permanent!) an API Key",
            security = @SecurityRequirement(name = "oauth", scopes = "auth"))
    @RequestMapping(method = RequestMethod.DELETE, path = PATH_KEY)
    public void revoke(Principal principal, @PathVariable String keyId) {
        service.revoke(principal.getName(), keyId);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            path = Constants.OAUTH_TOKEN_PATH,
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            params = {"client_id", "client_secret"})
    public OAuth2AccessTokenResponse grant(
            @RequestParam(name = "grant_type") AuthorizationGrantType grantType,
            @RequestParam(required = false) String scope,
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "client_secret") String clientSecret) {
        if(clientSecret.isEmpty()) clientSecret = null;
        if (!grantType.equals(AuthorizationGrantType.CLIENT_CREDENTIALS))
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.UNSUPPORTED_GRANT_TYPE));
        return service.authorize(clientId, clientSecret, scope);
    }
}
