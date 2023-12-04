/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.billingportal.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.UsageRecordCreateOnSubscriptionItemParams;
import com.stripe.param.billingportal.SessionCreateParams;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.UUID;

@XRayEnabled
public class StripeF {
    private final String price;

    public StripeF(String secret, String price) {
        Stripe.apiKey = secret;
        this.price = price;
    }

    public String create(UUID org, String email) throws StripeException {
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(email)
                .setMetadata(new HashMap<>(){{put("org-id", org.toString());}})
                .build();
        Customer cust = Customer.create(params);
        createSubscription(cust.getId());
        return cust.getId();
    }

    public String portal(String customerId) throws StripeException {
        if(customerId == null)
            throw new ErrorBuilder(HttpStatus.FORBIDDEN)
                    .message("Missing billing profile")
                    .exception();
        SessionCreateParams params = SessionCreateParams.builder()
                .setCustomer(customerId)
                .setReturnUrl("https://mytiki.com/reference")
                .build();
        Session session = Session.create(params);
        return session.getUrl();
    }

    public boolean isValid(String customerId) throws StripeException {
        Customer cust = Customer.retrieve(customerId);
        boolean hasPayment = cust.getInvoiceSettings().getDefaultPaymentMethod() != null;
        boolean delinquent = cust.getDelinquent();
        boolean hasSubscription = getSubscriptionItem(customerId) != null;
        return hasPayment && hasSubscription && !delinquent;
    }

    public void usage(String customerId, long records) throws StripeException {
        String itemId = getSubscriptionItem(customerId);
        if(itemId == null)
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED).message("Missing subscription").exception();
        UsageRecordCreateOnSubscriptionItemParams params = UsageRecordCreateOnSubscriptionItemParams.builder()
                .setQuantity(records)
                .setTimestamp(ZonedDateTime.now().toEpochSecond())
                .setAction(UsageRecordCreateOnSubscriptionItemParams.Action.INCREMENT)
                .build();
        UsageRecord.createOnSubscriptionItem(itemId, params, null);
    }

    private void createSubscription(String customerId) throws StripeException {
        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(SubscriptionCreateParams.Item.builder().setPrice(price).build())
                .build();
        Subscription.create(params);
    }

    private String getSubscriptionItem(String customerId) throws StripeException {
        SubscriptionCollection subscriptions = Subscription.list(
                SubscriptionListParams.builder().setCustomer(customerId).build());
        if(subscriptions != null) {
            for (Subscription subscription : subscriptions.autoPagingIterable()) {
                if(subscription.getStatus().equals("active") && subscription.getItems() != null) {
                    for (SubscriptionItem item : subscription.getItems().autoPagingIterable()) {
                        if (item.getPrice().getId().equals(price)) return item.getId();
                    }
                }
            }
        }
        return null;
    }
}
