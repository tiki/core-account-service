/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.exchange.github;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GithubConfig {

    @Bean
    public GithubClient githubClient(@Value("${com.mytiki.account.github.secret}") String githubSecret){
        return new GithubClient(githubSecret);
    }
}
