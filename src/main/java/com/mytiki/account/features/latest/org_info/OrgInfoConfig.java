/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.org_info;

import com.mytiki.account.utilities.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(OrgInfoConfig.PACKAGE_PATH)
@EntityScan(OrgInfoConfig.PACKAGE_PATH)
public class OrgInfoConfig {
    public static final String PACKAGE_PATH = Constants.PACKAGE_FEATURES_LATEST_DOT_PATH + ".org_info";

    @Bean
    public OrgInfoService orgInfoService(@Autowired OrgInfoRepository repository){
        return new OrgInfoService(repository);
    }

    @Bean
    public OrgInfoController orgInfoController(@Autowired OrgInfoService service){
        return new OrgInfoController(service);
    }
}
