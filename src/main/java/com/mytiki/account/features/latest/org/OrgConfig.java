/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.org;

import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.facade.StripeF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(OrgConfig.PACKAGE_PATH)
@EntityScan(OrgConfig.PACKAGE_PATH)
public class OrgConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".org";

    @Bean
    public OrgService orgService(@Autowired OrgRepository repository, @Autowired StripeF stripe){
        return new OrgService(repository, stripe);
    }
}
