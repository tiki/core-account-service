/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.stripe;

import com.mytiki.spring_rest_api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = StripeController.PATH_CONTROLLER)
public class StripeController {

    public static final String PATH_CONTROLLER = ApiConstants.API_LATEST_ROUTE + "stripe";

    private final StripeService service;

    public StripeController(StripeService service) {
        this.service = service;
    }

    @Operation(hidden = true)
    @RequestMapping(method = RequestMethod.POST, path = "/checkout-success")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void checkoutSuccess(
            @RequestHeader("Stripe-Signature") String signature,
            @RequestBody String body) {
        service.updateBilling(body, signature);
    }
}
