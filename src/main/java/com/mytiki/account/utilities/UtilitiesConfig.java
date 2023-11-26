/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities;


import com.mytiki.account.utilities.facade.SubscriptionF;
import com.mytiki.account.utilities.facade.SendgridF;
import com.mytiki.account.utilities.xray.XRayConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({
        PublicResolver.class,
        XRayConfig.class
})
public class UtilitiesConfig {
    @Bean
    public SendgridF sendgridFacade(@Value("${com.mytiki.account.sendgrid.apikey}") String sendgridApiKey) {
        return new SendgridF(sendgridApiKey);
    }

    @Bean
    public SubscriptionF subscriptionFacade(
            @Value("${com.mytiki.account.subscription.region}") String region,
            @Value("${com.mytiki.account.subscription.arn}") String arn) {
        return new SubscriptionF(region, arn);
    }
}
