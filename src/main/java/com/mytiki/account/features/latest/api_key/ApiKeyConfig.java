/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.api_key;

import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.facade.readme.ReadmeF;
import com.nimbusds.jose.JWSSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(ApiKeyConfig.PACKAGE_PATH)
@EntityScan(ApiKeyConfig.PACKAGE_PATH)
public class ApiKeyConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".api_key";

    @Bean
    public ApiKeyService apiKeyService(
            @Autowired ApiKeyRepository repository,
            @Autowired JWSSigner signer,
            @Autowired ReadmeF readme){
        return new ApiKeyService(repository, signer, readme);
    }

    @Bean
    public ApiKeyController apiKeyController(@Autowired ApiKeyService service){
        return new ApiKeyController(service);
    }
}
