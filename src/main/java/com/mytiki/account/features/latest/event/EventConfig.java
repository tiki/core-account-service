/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.subscription.SubscriptionService;
import com.mytiki.account.utilities.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.HashMap;
import java.util.Map;

@EnableJpaRepositories(EventConfig.PACKAGE_PATH)
@EntityScan(EventConfig.PACKAGE_PATH)
public class EventConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".event";

    @Bean
    public EventCallback eventCallback(@Autowired EventHandler handler){
        return new EventCallback(handler);
    }

    @Bean
    public EventService eventService(
            @Autowired @Qualifier(value = "eventArns") Map<String, String> arns,
            @Autowired EventRepository repository,
            @Value("${com.mytiki.account.event.region}") String region,
            @Autowired ObjectMapper mapper){
        return new EventService(arns, repository, region, mapper);
    }

    @Bean EventHandler eventHandler(
            @Autowired EventRepository repository,
            @Autowired ObjectMapper mapper,
            @Autowired SubscriptionService subscriptionService){
        return new EventHandler(repository, mapper, subscriptionService);
    }

    @Bean(name = "eventArns")
    @ConfigurationProperties(prefix = "com.mytiki.account.event.arns")
    public Map<String, String> eventArns() { return new HashMap<>(); }
}
