/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class UtilitiesConfig {
    @Bean
    public Sendgrid sendgrid(@Value("${com.mytiki.account.sendgrid.apikey}") String sendgridApiKey) {
        return new Sendgrid(sendgridApiKey);
    }
}
