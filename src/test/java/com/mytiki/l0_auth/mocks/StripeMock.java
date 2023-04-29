/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.mocks;

import com.stripe.net.Webhook;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class StripeMock {

    public static String checkoutSessionCompleted(String customerId, String clientReferenceId){
        return "{\n" +
                "  \"id\": \"evt_1My30RDveWor0wgF97grC1uB\",\n" +
                "  \"object\": \"event\",\n" +
                "  \"api_version\": \"2022-11-15\",\n" +
                "  \"created\": 1681780011,\n" +
                "  \"data\": {\n" +
                "    \"object\": {\n" +
                "      \"id\": \"cs_test_b1JXa9roZkDSKRceb4xdgU3GTeHvQnMmn1sZ7oQaZWrXzVY1cT7bhRk3mQ\",\n" +
                "      \"object\": \"checkout.session\",\n" +
                "      \"after_expiration\": null,\n" +
                "      \"allow_promotion_codes\": null,\n" +
                "      \"amount_subtotal\": 0,\n" +
                "      \"amount_total\": 0,\n" +
                "      \"automatic_tax\": {\n" +
                "        \"enabled\": true,\n" +
                "        \"status\": \"complete\"\n" +
                "      },\n" +
                "      \"billing_address_collection\": null,\n" +
                "      \"cancel_url\": \"https://console.mytiki.com/billing\",\n" +
                "      \"client_reference_id\": \"" + clientReferenceId + "\",\n" +
                "      \"consent\": null,\n" +
                "      \"consent_collection\": null,\n" +
                "      \"created\": 1681779953,\n" +
                "      \"currency\": \"usd\",\n" +
                "      \"currency_conversion\": null,\n" +
                "      \"custom_fields\": [\n" +
                "      ],\n" +
                "      \"custom_text\": {\n" +
                "        \"shipping_address\": null,\n" +
                "        \"submit\": null\n" +
                "      },\n" +
                "      \"customer\": \"" + customerId + "\",\n" +
                "      \"customer_creation\": \"always\",\n" +
                "      \"customer_details\": {\n" +
                "        \"address\": {\n" +
                "          \"city\": \"Smyrna\",\n" +
                "          \"country\": \"US\",\n" +
                "          \"line1\": \"420 Blazer Avenue\",\n" +
                "          \"line2\": null,\n" +
                "          \"postal_code\": \"37167\",\n" +
                "          \"state\": \"TN\"\n" +
                "        },\n" +
                "        \"email\": \"howdymaudi@gmail.com\",\n" +
                "        \"name\": \"Ben Dover\",\n" +
                "        \"phone\": null,\n" +
                "        \"tax_exempt\": \"none\",\n" +
                "        \"tax_ids\": [\n" +
                "        ]\n" +
                "      },\n" +
                "      \"customer_email\": null,\n" +
                "      \"expires_at\": 1681866353,\n" +
                "      \"invoice\": \"in_1My30PDveWor0wgF4U2LL97m\",\n" +
                "      \"invoice_creation\": null,\n" +
                "      \"livemode\": false,\n" +
                "      \"locale\": null,\n" +
                "      \"metadata\": {\n" +
                "      },\n" +
                "      \"mode\": \"subscription\",\n" +
                "      \"payment_intent\": null,\n" +
                "      \"payment_link\": null,\n" +
                "      \"payment_method_collection\": \"always\",\n" +
                "      \"payment_method_options\": {\n" +
                "        \"us_bank_account\": {\n" +
                "          \"verification_method\": \"automatic\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"payment_method_types\": [\n" +
                "        \"card\",\n" +
                "        \"link\",\n" +
                "        \"us_bank_account\",\n" +
                "        \"cashapp\"\n" +
                "      ],\n" +
                "      \"payment_status\": \"paid\",\n" +
                "      \"phone_number_collection\": {\n" +
                "        \"enabled\": false\n" +
                "      },\n" +
                "      \"recovered_from\": null,\n" +
                "      \"setup_intent\": null,\n" +
                "      \"shipping_address_collection\": null,\n" +
                "      \"shipping_cost\": null,\n" +
                "      \"shipping_details\": null,\n" +
                "      \"shipping_options\": [\n" +
                "      ],\n" +
                "      \"status\": \"complete\",\n" +
                "      \"submit_type\": null,\n" +
                "      \"subscription\": \"sub_1My30PDveWor0wgFN8KEYmD9\",\n" +
                "      \"success_url\": \"https://console.mytiki.com/billing\",\n" +
                "      \"total_details\": {\n" +
                "        \"amount_discount\": 0,\n" +
                "        \"amount_shipping\": 0,\n" +
                "        \"amount_tax\": 0\n" +
                "      },\n" +
                "      \"url\": null\n" +
                "    }\n" +
                "  },\n" +
                "  \"livemode\": false,\n" +
                "  \"pending_webhooks\": 1,\n" +
                "  \"request\": {\n" +
                "    \"id\": null,\n" +
                "    \"idempotency_key\": null\n" +
                "  },\n" +
                "  \"type\": \"checkout.session.completed\"\n" +
                "}";
    }

    public static String generateSignature(String secret, String payload)
            throws NoSuchAlgorithmException, InvalidKeyException {
        long timestamp = Webhook.Util.getTimeNow();
        String payloadToSign = String.format("%d.%s", timestamp, payload);
        String signature = Webhook.Util.computeHmacSha256(secret, payloadToSign);
        return String.format("t=%d,%s=%s", timestamp, Webhook.Signature.EXPECTED_SCHEME, signature);
    }
}
