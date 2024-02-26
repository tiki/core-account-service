/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@XRayEnabled
@Tag(name = "Managing Data Subscriptions")
@RestController
@RequestMapping(value = SubscriptionController.ROUTE)
public class SubscriptionController {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "subscription";

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-subscription-list",
            summary = "List Subscriptions",
            description = "Returns a filterable list of data subscriptions",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @RequestMapping(method = RequestMethod.GET)
    @Secured("SCOPE_account:admin")
    public List<SubscriptionAO> list(JwtAuthenticationToken token, @RequestParam(required = false) String status) {
        return service.list(new OauthSub(token.getName()), status);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-subscription-get",
            summary = "Get Subscription",
            description = "Returns the current status for a data subscription",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @RequestMapping(method = RequestMethod.GET, path = "/{subscription-id}")
    @Secured("SCOPE_account:admin")
    public SubscriptionAORsp get(
            JwtAuthenticationToken token,
            @PathVariable(name = "subscription-id") String subscriptionId) {
        return service.get(new OauthSub(token.getName()), subscriptionId);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-subscription-estimate",
            summary = "Create Estimate",
            description = "Creates a new estimate for a data subscription",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @RequestMapping(method = RequestMethod.POST)
    @Secured("SCOPE_account:admin")
    public SubscriptionAORsp post(JwtAuthenticationToken token, @RequestBody SubscriptionAOReq body) {
        return service.estimate(new OauthSub(token.getName()), body);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-subscription-purchase",
            summary = "Purchase Subscription",
            description = "Converts an estimate into a paid data subscription.",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @RequestMapping(method = RequestMethod.POST, path = "/{subscription-id}/purchase")
    @Secured("SCOPE_account:admin")
    public SubscriptionAORsp purchase(
            JwtAuthenticationToken token,
            @PathVariable(name = "subscription-id") String subscriptionId) {
        return service.purchase(new OauthSub(token.getName()), subscriptionId);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-subscription-pause",
            summary = "Pause Subscription",
            description = "Pauses an existing data subscription",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @RequestMapping(method = RequestMethod.POST, path = "/{subscription-id}/pause")
    @Secured("SCOPE_account:admin")
    public SubscriptionAO pause(
            JwtAuthenticationToken token,
            @PathVariable(name = "subscription-id") String subscriptionId) {
        return service.pause(new OauthSub(token.getName()), subscriptionId);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-subscription-restart",
            summary = "Restart Subscription",
            description = "Restart an paused data subscription",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @RequestMapping(method = RequestMethod.POST, path = "/{subscription-id}/restart")
    @Secured("SCOPE_account:admin")
    public SubscriptionAO restart(
            JwtAuthenticationToken token,
            @PathVariable(name = "subscription-id") String subscriptionId) {
        return service.restart(new OauthSub(token.getName()), subscriptionId);
    }
}
