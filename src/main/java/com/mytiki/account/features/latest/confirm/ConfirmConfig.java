/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.confirm;

import com.mytiki.account.features.latest.api_key.ApiKeyConfig;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.facade.TemplateF;
import com.mytiki.account.utilities.facade.SendgridF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@EnableJpaRepositories(ConfirmConfig.PACKAGE_PATH)
@EntityScan(ConfirmConfig.PACKAGE_PATH)
public class ConfirmConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".confirm";

    @Bean
    public ConfirmService confirmService(
            @Autowired SendgridF sendgrid,
            @Autowired @Qualifier("confirmMustache") TemplateF mustache,
            @Autowired ConfirmRepository repository) {
        return new ConfirmService(sendgrid, mustache, repository);
    }

    @Bean(name = "confirmMustache")
    public TemplateF confirmMustache() {
        TemplateF mustache = new TemplateF();
        mustache.load("otp"); //TODO FIX ME.
        return mustache;
    }
}
