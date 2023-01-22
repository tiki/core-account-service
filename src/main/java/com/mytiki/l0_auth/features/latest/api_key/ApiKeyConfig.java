/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.api_key;

import com.mytiki.l0_auth.features.latest.app_info.AppInfoService;
import com.mytiki.l0_auth.features.latest.refresh.RefreshService;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoService;
import com.mytiki.l0_auth.security.OauthScopes;
import com.mytiki.l0_auth.utilities.Constants;
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
    public static final String PACKAGE_PATH = Constants.PACKAGE_FEATURES_LATEST_DOT_PATH + ".api_key";

    @Bean
    public ApiKeyController apiKeyController(@Autowired ApiKeyService service){
        return new ApiKeyController(service);
    }

    @Bean
    public ApiKeyService apiKeyService(
            @Autowired ApiKeyRepository repository,
            @Autowired UserInfoService userInfoService,
            @Autowired AppInfoService appInfoService,
            @Autowired RefreshService refreshService,
            @Autowired JWSSigner signer,
            @Autowired OauthScopes allowedScopes,
            @Value("${com.mytiki.l0_auth.oauth.client_credentials.public_scopes}") List<String> publicScopes){
        return new ApiKeyService(repository, userInfoService, appInfoService, refreshService, signer,
                allowedScopes, publicScopes);
    }
}
