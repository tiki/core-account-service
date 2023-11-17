/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.auth_code.google;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleConfig {

    @Bean
    public GoogleClient googleClient(
            @Value("${com.mytiki.account.google.id}") String id,
            @Value("${com.mytiki.account.google.secret}") String secret) {
        return new GoogleClient(id, secret);
    }
}
