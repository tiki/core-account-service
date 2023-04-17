/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.org_info;

import com.mytiki.l0_auth.features.latest.user_info.UserInfoService;
import com.mytiki.l0_auth.utilities.Constants;
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
    public OrgInfoController orgInfoController(@Autowired OrgInfoService service,
                                               @Autowired UserInfoService userInfoService){
        return new OrgInfoController(service, userInfoService);
    }
}
