/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.api_key;

import com.mytiki.spring_rest_api.ApiConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "ACCOUNT")
@RestController
@RequestMapping(value = ApiConstants.API_LATEST_ROUTE)
public class ApiKeyController {
    public static final String PATH_KEY = "key/{keyId}";
    public static final String PATH_APP_KEY = "app/{appId}/key";

    private final ApiKeyService service;

    public ApiKeyController(ApiKeyService service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.GET, path = PATH_APP_KEY)
    public List<ApiKeyAO> getAppKeys(Principal principal, @PathVariable String appId) {
        return service.getByAppId(principal.getName(), appId);
    }

    @RequestMapping(method = RequestMethod.POST, path = PATH_APP_KEY)
    public ApiKeyAOCreate createAppKey(
            Principal principal,
            @PathVariable String appId,
            @RequestParam(required = false) boolean isPublic) {
        return service.create(principal.getName(), appId, isPublic);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = PATH_KEY)
    public void revoke(Principal principal, @PathVariable String keyId) {
        service.revoke(principal.getName(), keyId);
    }
}
