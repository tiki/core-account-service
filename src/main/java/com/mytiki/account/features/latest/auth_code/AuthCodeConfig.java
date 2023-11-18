/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.auth_code;

import com.mytiki.account.features.latest.auth_code.github.GithubClient;
import com.mytiki.account.features.latest.auth_code.github.GithubConfig;
import com.mytiki.account.features.latest.auth_code.google.GoogleClient;
import com.mytiki.account.features.latest.auth_code.google.GoogleConfig;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.features.latest.readme.ReadmeService;
import com.mytiki.account.features.latest.refresh.RefreshService;
import com.nimbusds.jose.JWSSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({
        GithubConfig.class,
        GoogleConfig.class
})
@Configuration
public class AuthCodeConfig {
    @Bean
    public AuthCodeService authCodeService(
            @Autowired ProfileService profileService,
            @Autowired RefreshService refreshService,
            @Autowired JWSSigner signer,
            @Autowired ReadmeService readme,
            @Autowired GoogleClient google,
            @Autowired GithubClient github) {
        return new AuthCodeService(profileService, refreshService, signer, readme, google, github);
    }
}
