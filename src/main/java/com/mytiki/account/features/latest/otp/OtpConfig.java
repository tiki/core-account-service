/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.otp;

import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.features.latest.user_info.UserInfoService;
import com.mytiki.account.security.oauth.OauthScopes;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.facade.TemplateF;
import com.mytiki.account.utilities.facade.readme.ReadmeF;
import com.mytiki.account.utilities.facade.SendgridF;
import com.nimbusds.jose.JWSSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(OtpConfig.PACKAGE_PATH)
@EntityScan(OtpConfig.PACKAGE_PATH)
public class OtpConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".otp";
    public static final String TEMPLATE = "otp";

    @Bean
    public OtpService otpService(
            @Autowired OtpRepository otpRepository,
            @Autowired @Qualifier("otpMustache") TemplateF mustache,
            @Autowired SendgridF sendgrid,
            @Autowired JWSSigner signer,
            @Autowired RefreshService refreshService,
            @Autowired UserInfoService userInfoService,
            @Autowired ReadmeF readme) {
        return new OtpService(otpRepository, mustache, sendgrid, signer, refreshService, userInfoService, readme);
    }

    @Bean
    public OtpController otpController(@Autowired OtpService otpService) {
        return new OtpController(otpService);
    }

    @Bean(name = "otpMustache")
    public TemplateF otpMustache() {
        TemplateF mustache = new TemplateF();
        mustache.load(TEMPLATE);
        return mustache;
    }
}
