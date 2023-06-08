/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.exchange.shopify;

import com.mytiki.l0_auth.features.latest.exchange.ExchangeClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.web.client.RestTemplate;

public class ShopifyClient implements ExchangeClient {

    @Override
    public String validate(String clientId, String token) {
        try {
            RestTemplate client = new RestTemplateBuilder()
                    .rootUri("https://" + clientId)
                    .defaultHeader("X-Shopify-Access-Token", token)
                    .build();
            ShopifyAO rsp = client.getForObject("/admin/api/2023-04/shop.json", ShopifyAO.class);
            if (rsp != null) {
                ShopifyAOShop shop = rsp.getShop();
                if (shop.getEmail() != null) return rsp.getShop().getEmail();
            }
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.ACCESS_DENIED),
                    "client_id and/or subject_token_type are invalid");
        } catch (Exception e) {
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "Issue with token exchange",
                    null
            ), e);
        }
    }
}
