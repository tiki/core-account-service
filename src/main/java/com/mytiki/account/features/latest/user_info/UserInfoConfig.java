/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.user_info;

import com.mytiki.account.features.latest.confirm.ConfirmService;
import com.mytiki.account.features.latest.org_info.OrgInfoService;
import com.mytiki.account.utilities.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(UserInfoConfig.PACKAGE_PATH)
@EntityScan(UserInfoConfig.PACKAGE_PATH)
public class UserInfoConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".user_info";

    @Bean
    public UserInfoController userInfoController(@Autowired UserInfoService service){
        return new UserInfoController(service);
    }

    @Bean
    public UserInfoService userInfoService(@Autowired UserInfoRepository repository,
                                           @Autowired OrgInfoService orgInfoService,
                                           @Autowired ConfirmService confirm){
        return new UserInfoService(repository, orgInfoService, confirm);
    }
}
