/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.api_key;

import com.mytiki.account.features.latest.app_info.AppInfoService;
import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.security.oauth.OauthInternal;
import com.mytiki.account.security.oauth.OauthScopes;
import com.mytiki.account.utilities.Constants;
import com.nimbusds.jose.JWSSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@EnableJpaRepositories(ApiKeyConfig.PACKAGE_PATH)
@EntityScan(ApiKeyConfig.PACKAGE_PATH)
public class ApiKeyConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".api_key";

    @Bean
    public ApiKeyController apiKeyController(
            @Autowired ApiKeyService service,
            @Autowired AppInfoService appInfoService){
        return new ApiKeyController(service, appInfoService);
    }

    @Bean
    public ApiKeyService apiKeyService(
            @Autowired ApiKeyRepository repository,
            @Autowired AppInfoService appInfoService,
            @Autowired RefreshService refreshService,
            @Autowired JWSSigner signer,
            @Autowired OauthScopes allowedScopes,
            @Value("${com.mytiki.account.oauth.client_credentials.public.scopes}") List<String> publicScopes,
            @Autowired OauthInternal oauthInternal){
        return new ApiKeyService(repository, appInfoService, refreshService, signer,
                allowedScopes, publicScopes, oauthInternal);
    }
}
