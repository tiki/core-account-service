/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.provider_user;

import com.mytiki.account.features.latest.provider.ProviderService;
import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.utilities.Constants;
import com.nimbusds.jose.JWSSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@EnableJpaRepositories(ProviderUserConfig.PACKAGE_PATH)
@EntityScan(ProviderUserConfig.PACKAGE_PATH)
public class ProviderUserConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".provider_user";

    @Bean
    ProviderUserService providerUserService(
            @Autowired ProviderUserRepository repository,
            @Autowired ProviderService providerService,
            @Autowired RefreshService refreshService,
            @Autowired JWSSigner signer,
            @Value("${com.mytiki.account.oauth.client_credentials.public.scopes}") List<String> publicScopes) {
        return new ProviderUserService(repository, providerService, refreshService, signer, publicScopes);
    }

    @Bean
    ProviderUserController providerUserController(@Autowired ProviderUserService service) {
        return new ProviderUserController(service);
    }
}
