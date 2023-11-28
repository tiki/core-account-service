/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import com.mytiki.account.features.latest.cleanroom.CleanroomService;
import com.mytiki.account.features.latest.ocean.OceanService;
import com.mytiki.account.utilities.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(SubscriptionConfig.PACKAGE_PATH)
@EntityScan(SubscriptionConfig.PACKAGE_PATH)
public class SubscriptionConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".subscription";

    @Bean
    public SubscriptionController subscriptionController(@Autowired SubscriptionService service) {
        return new SubscriptionController(service);
    }

    @Bean
    public SubscriptionService subscriptionService(
            @Autowired SubscriptionRepository repository,
            @Autowired OceanService oceanService,
            @Autowired CleanroomService cleanroomService){
        return new SubscriptionService(repository, oceanService, cleanroomService);
    }
}
