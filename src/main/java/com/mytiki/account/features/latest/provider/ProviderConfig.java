/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.facade.SqsF;
import com.nimbusds.jose.JWSSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(ProviderConfig.PACKAGE_PATH)
@EntityScan(ProviderConfig.PACKAGE_PATH)
public class ProviderConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".provider";

    @Bean
    public ProviderService providerService(
            @Autowired ProviderRepository repository,
            @Autowired ProfileService profileService,
            @Autowired JWSSigner signer,
            @Value("${com.mytiki.account.trail.sqs.region}") String region,
            @Value("${com.mytiki.account.trail.sqs.url}") String url,
            @Autowired ObjectMapper mapper) {
        SqsF trail = new SqsF(region, url);
        return new ProviderService(repository, profileService, signer, trail, mapper);
    }

    @Bean
    public ProviderController providerController(@Autowired ProviderService service) {
        return new ProviderController(service);
    }
}
