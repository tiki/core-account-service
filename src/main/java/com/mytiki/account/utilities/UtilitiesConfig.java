/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities;


import com.mytiki.account.utilities.facade.SendgridF;
import com.mytiki.account.utilities.facade.StripeF;
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
    public StripeF stripeFacade(
            @Value("${com.mytiki.account.stripe.secret}") String secret,
            @Value("${com.mytiki.account.stripe.price}") String price){
        return new StripeF(secret, price);
    }
}
