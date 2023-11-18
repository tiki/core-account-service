/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.readme;

import com.mytiki.account.features.latest.api_key.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class ReadmeConfig {

    @Bean
    public ReadmeController readmeController(@Autowired ReadmeService service) {
        return new ReadmeController(service);
    }

    @Bean
    public ReadmeService readmeService(
            @Value("${com.mytiki.account.readme.secret}") String secret,
            @Autowired ApiKeyService apiKeyService) {
        return new ReadmeService(secret, apiKeyService);
    }
}
