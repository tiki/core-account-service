/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.provider.ProviderAO;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@XRayEnabled
@Tag(name = "Data Purchaser")
@RestController
@RequestMapping(value = SubscriptionController.ROUTE)
public class SubscriptionController {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "subscription";

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-subscription-get",
            summary = "Get Subscription",
            description = "Returns the current status for a data subscription",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @RequestMapping(method = RequestMethod.GET, path = "/{subscription-id}")
    @Secured({"SCOPE_account:admin", "SCOPE_account:internal:read"})
    public SubscriptionAO get(
            JwtAuthenticationToken token,
            @PathVariable(name = "subscription-id") String subscriptionId) {
        return service.get(new OauthSub(token.getName()), subscriptionId);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-subscription-estimate",
            summary = "Create Estimate",
            description = "Creates a new estimate for a data subscription",
            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
    @RequestMapping(method = RequestMethod.POST)
    @Secured({"SCOPE_account:admin", "SCOPE_account:internal:read"})
    public SubscriptionAO post(JwtAuthenticationToken token, @RequestBody SubscriptionAOReq body) {
        return service.estimate(new OauthSub(token.getName()), body);
    }

//    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-subscription-purchase",
//            summary = "Purchase Subscription",
//            description = "Purchases a data subscription by converting an estimate",
//            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
//    @RequestMapping(method = RequestMethod.POST, path = "/{subscription-id}/purchase")
//    @Secured({"SCOPE_account:admin", "SCOPE_account:internal:read"})
//    public SubscriptionAO purchase(JwtAuthenticationToken token) {
//        return service.estimate(new OauthSub(token.getName()), body);
//    }
//
//    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-subscription-pause",
//            summary = "Pause Subscription",
//            description = "Pauses an existing data subscription",
//            security = @SecurityRequirement(name = "default", scopes = "account:admin"))
//    @RequestMapping(method = RequestMethod.POST, path = "/{subscription-id}/pause")
//    @Secured({"SCOPE_account:admin", "SCOPE_account:internal:read"})
//    public SubscriptionAO pause(JwtAuthenticationToken token) {
//        return service.estimate(new OauthSub(token.getName()), body);
//    }
}
