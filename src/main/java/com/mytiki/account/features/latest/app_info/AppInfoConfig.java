/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.app_info;

import com.mytiki.account.features.latest.user_info.UserInfoService;
import com.mytiki.account.utilities.Constants;
import com.nimbusds.jose.JWSSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(AppInfoConfig.PACKAGE_PATH)
@EntityScan(AppInfoConfig.PACKAGE_PATH)
public class AppInfoConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".app_info";

    @Bean
    public AppInfoService appInfoService(
            @Autowired AppInfoRepository repository,
            @Autowired UserInfoService userInfoService,
            @Autowired JWSSigner signer){
        return new AppInfoService(repository, userInfoService, signer);
    }

    @Bean
    public AppInfoController appInfoController(@Autowired AppInfoService service){
        return new AppInfoController(service);
    }
}
