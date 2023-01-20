/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.user_info;

import com.mytiki.l0_auth.utilities.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@EnableJpaRepositories(UserInfoConfig.PACKAGE_PATH)
@EntityScan(UserInfoConfig.PACKAGE_PATH)
public class UserInfoConfig {
    public static final String PACKAGE_PATH = Constants.PACKAGE_FEATURES_LATEST_DOT_PATH + ".user_info";

    @Bean
    public UserInfoController userInfoController(
            @Autowired UserInfoService service,
            @Autowired JwtDecoder jwtDecoder){
        return new UserInfoController(service, jwtDecoder);
    }

    @Bean
    public UserInfoService userInfoService(@Autowired UserInfoRepository repository){
        return new UserInfoService(repository);
    }
}
