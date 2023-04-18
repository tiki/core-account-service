/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.stripe;

import com.mytiki.l0_auth.features.latest.org_info.OrgInfoService;
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
            @Value("${com.mytiki.l0_auth.stripe.signing_secret}") String stripeSecret){
        return new StripeService(orgInfoService, stripeSecret);
    }
}
