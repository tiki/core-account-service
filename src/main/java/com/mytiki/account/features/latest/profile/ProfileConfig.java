/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.profile;

import com.mytiki.account.features.latest.api_key.ApiKeyService;
import com.mytiki.account.features.latest.confirm.ConfirmService;
import com.mytiki.account.features.latest.org.OrgService;
import com.mytiki.account.features.latest.oauth.OauthScopes;
import com.mytiki.account.utilities.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(ProfileConfig.PACKAGE_PATH)
@EntityScan(ProfileConfig.PACKAGE_PATH)
public class ProfileConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".profile";

    @Bean
    public ProfileController profileController(@Autowired ProfileService service){
        return new ProfileController(service);
    }

    @Bean
    public ProfileService profileService(@Autowired ProfileRepository repository,
                                          @Autowired OrgService orgService,
                                          @Autowired ConfirmService confirmService,
                                          @Autowired ApiKeyService apiKeyService,
                                          @Autowired OauthScopes allowedScopes){
        return new ProfileService(repository, orgService, confirmService, apiKeyService, allowedScopes);
    }
}
