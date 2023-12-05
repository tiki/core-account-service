/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.facade.StripeF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(OceanConfig.PACKAGE_PATH)
@EntityScan(OceanConfig.PACKAGE_PATH)
public class OceanConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".ocean";

    @Bean
    public OceanController oceanController(@Autowired OceanService service){
        return new OceanController(service);
    }

    @Bean
    public OceanService oceanService(
            @Autowired OceanAws aws,
            @Value("${com.mytiki.account.cleanroom.bucket}") String bucket,
            @Autowired ObjectMapper mapper,
            @Autowired OceanRepository repository,
            @Autowired StripeF stripe){
        return new OceanService(aws, bucket, mapper, repository, stripe);
    }

    @Bean
    public OceanAws oceanAws(
            @Value("${com.mytiki.account.ocean.region}") String region,
            @Value("${com.mytiki.account.ocean.arn}") String arn,
            @Autowired ObjectMapper mapper){
        return new OceanAws(region, arn, mapper);
    }
}
