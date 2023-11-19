/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.refresh;

import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.features.latest.readme.ReadmeService;
import com.mytiki.account.utilities.Constants;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@EnableJpaRepositories(RefreshConfig.PACKAGE_PATH)
@EntityScan(RefreshConfig.PACKAGE_PATH)
public class RefreshConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".refresh";

    @Bean
    public RefreshService refreshService(
            @Autowired RefreshRepository repository,
            @Autowired JWSSigner signer,
            @Autowired JWTProcessor<SecurityContext> jwtProcessor,
            @Autowired ReadmeService readme,
            @Autowired ProfileService profileService) {
        return new RefreshService(
                repository, signer, new NimbusJwtDecoder(jwtProcessor), readme, profileService);
    }
}
