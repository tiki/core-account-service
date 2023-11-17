/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.auth_code.github;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GithubConfig {

    @Bean
    public GithubClient githubClient(
            @Value("${com.mytiki.account.github.id}") String id,
            @Value("${com.mytiki.account.github.secret}") String secret){
        return new GithubClient(id, secret);
    }
}
