/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.exchange.shopify;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShopifyConfig {
    @Bean
    public ShopifyClient shopifyClient() {
        return new ShopifyClient();
    }
}
