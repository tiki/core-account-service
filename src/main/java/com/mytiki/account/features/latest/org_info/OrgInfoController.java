/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.org_info;

import com.mytiki.account.features.latest.user_info.UserInfoService;
import com.mytiki.account.security.oauth.OauthScopes;
import com.mytiki.account.utilities.Constants;
import com.mytiki.spring_rest_api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "")
@RestController
@RequestMapping(value = OrgInfoController.PATH_CONTROLLER)
public class OrgInfoController {
    public static final String PATH_CONTROLLER = ApiConstants.API_LATEST_ROUTE + "org";

    private final OrgInfoService service;
    private final UserInfoService userInfoService;

    public OrgInfoController(OrgInfoService service, UserInfoService userInfoService) {
        this.service = service;
        this.userInfoService = userInfoService;
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-org-get",
            summary = "Get Org",
            description = "Get an org's profile",
            security = @SecurityRequirement(name = "oauth", scopes = "auth"))
    @RequestMapping(method = RequestMethod.GET, path = "/{orgId}")
    public OrgInfoAO get(JwtAuthenticationToken token, @PathVariable String orgId) {
        if(OauthScopes.hasScope(token, "internal:read"))
            return service.get(orgId);
        else return service.getForUser(token.getName(), orgId);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-org-post-user",
            summary = "Add to Org",
            description = "Add a user to an org",
            security = @SecurityRequirement(name = "oauth", scopes = "auth"))
    @RequestMapping(method = RequestMethod.POST, path = "/{orgId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void addToOrg(Principal principal, @PathVariable String orgId, @RequestBody OrgInfoAOReq body) {
        userInfoService.addToOrg(principal.getName(), orgId, body.getEmail());
    }
}
