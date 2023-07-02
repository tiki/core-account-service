/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.stripe;

import com.mytiki.account.features.latest.org_info.OrgInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class StripeConfig {

    @Bean
    public StripeController stripeController(@Autowired StripeService service){
        return new StripeController(service);
    }

    @Bean
    public StripeService stripeService(
            @Autowired OrgInfoService orgInfoService,
            @Value("${com.mytiki.account.stripe.signing_secret}") String stripeSecret){
        return new StripeService(orgInfoService, stripeSecret);
    }
}
