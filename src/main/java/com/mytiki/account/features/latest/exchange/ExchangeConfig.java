/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.exchange;

import com.mytiki.account.features.latest.exchange.shopify.ShopifyClient;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.features.latest.readme.ReadmeService;
import com.mytiki.account.features.latest.refresh.RefreshService;
import com.nimbusds.jose.JWSSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({ShopifyClient.class})
@Configuration
public class ExchangeConfig {
    @Bean
    public ExchangeService exchangeService(
            @Autowired ProfileService profileService,
            @Autowired RefreshService refreshService,
            @Autowired JWSSigner signer,
            @Autowired ReadmeService readme,
            @Autowired ShopifyClient shopify) {
        return new ExchangeService(profileService, refreshService, signer, readme, shopify);
    }
}
