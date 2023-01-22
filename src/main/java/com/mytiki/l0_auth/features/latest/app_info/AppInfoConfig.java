/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.app_info;

import com.mytiki.l0_auth.utilities.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(AppInfoConfig.PACKAGE_PATH)
@EntityScan(AppInfoConfig.PACKAGE_PATH)
public class AppInfoConfig {
    public static final String PACKAGE_PATH = Constants.PACKAGE_FEATURES_LATEST_DOT_PATH + ".app_info";

    @Bean
    public AppInfoService appInfoService(@Autowired AppInfoRepository repository){
        return new AppInfoService(repository);
    }

    @Bean
    public AppInfoController appInfoController(@Autowired AppInfoService service){
        return new AppInfoController(service);
    }
}