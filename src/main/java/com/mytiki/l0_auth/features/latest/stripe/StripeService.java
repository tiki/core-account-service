/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.stripe;

import com.mytiki.l0_auth.features.latest.org_info.OrgInfoService;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.http.HttpStatus;

public class StripeService {

    private final String stripeSecret;
    private final OrgInfoService orgInfoService;

    public StripeService(OrgInfoService orgInfoService, String stripeSecret) {
        this.orgInfoService = orgInfoService;
        this.stripeSecret = stripeSecret;
    }

    public Event guardSignature(String req, String signature){
        try {
           return Webhook.constructEvent(req, signature, stripeSecret);
        } catch (SignatureVerificationException e) {
            throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN)
                    .message("Signature verification failed")
                    .build();
        }
    }

    public void updateBilling(String req, String signature){
        Event event = guardSignature(req, signature);
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if(dataObjectDeserializer.getObject().isPresent()){
            if(event.getType().equals("checkout.session.completed")) {
                Session session = (Session) dataObjectDeserializer.getObject().get();
                orgInfoService.setBilling(session.getClientReferenceId(), session.getCustomer());
            }else
                throw new ApiExceptionBuilder(HttpStatus.BAD_REQUEST)
                        .message("Invalid event type")
                        .properties("type", event.getType())
                        .build();
        }else
            throw new ApiExceptionBuilder(HttpStatus.BAD_REQUEST)
                    .message("Failed to deserialize event")
                    .build();
    }
}
