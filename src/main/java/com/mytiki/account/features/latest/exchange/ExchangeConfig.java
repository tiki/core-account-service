/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.exchange;

import com.mytiki.account.features.latest.exchange.github.GithubClient;
import com.mytiki.account.features.latest.exchange.github.GithubConfig;
import com.mytiki.account.features.latest.exchange.google.GoogleClient;
import com.mytiki.account.features.latest.exchange.shopify.ShopifyClient;
import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.features.latest.oauth.OauthScopes;
import com.mytiki.account.utilities.facade.readme.ReadmeF;
import com.nimbusds.jose.JWSSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({
        GithubConfig.class,
        GoogleClient.class,
        ShopifyClient.class
})
@Configuration
public class ExchangeConfig {
    @Bean
    public ExchangeService exchangeService(
            @Autowired ProfileService profileService,
            @Autowired RefreshService refreshService,
            @Autowired JWSSigner signer,
            @Autowired OauthScopes allowedScopes,
            @Autowired ReadmeF readme,
            @Autowired GoogleClient google,
            @Autowired GithubClient github,
            @Autowired ShopifyClient shopify) {
        return new ExchangeService(profileService, refreshService, signer,
                allowedScopes, readme, google, github, shopify);
    }
}
