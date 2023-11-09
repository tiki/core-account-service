/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.org_info;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@XRayEnabled
@Tag(name = "")
@RestController
@RequestMapping(value = OrgInfoController.ROUTE)
public class OrgInfoController {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "org";

    private final OrgInfoService service;

    public OrgInfoController(OrgInfoService service) {
        this.service = service;
    }

    @Operation(
            hidden = true,
            operationId = Constants.PROJECT_DASH_PATH +  "-org-get",
            summary = "Get Org",
            description = "Retrieve the authorized User's Org details",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.GET)
    public OrgInfoAO get(JwtAuthenticationToken token) {
        return service.getByUser(token.getName());
    }
}
