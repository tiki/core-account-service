/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.otp;

import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.features.latest.user_info.UserInfoService;
import com.mytiki.account.security.oauth.OauthScopes;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.facade.MustacheF;
import com.mytiki.account.utilities.facade.ReadmeF;
import com.mytiki.account.utilities.facade.SendgridF;
import com.nimbusds.jose.JWSSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@EnableJpaRepositories(OtpConfig.PACKAGE_PATH)
@EntityScan(OtpConfig.PACKAGE_PATH)
public class OtpConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".otp";
    public static final String TEMPLATE_BODY_HTML = "otp-body-html.mustache";
    public static final String TEMPLATE_BODY_TXT = "otp-body-txt.mustache";
    public static final String TEMPLATE_SUBJECT = "otp-subject.mustache";

    @Bean
    public OtpService otpService(
            @Autowired OtpRepository otpRepository,
            @Autowired @Qualifier("otpMustache") MustacheF templates,
            @Autowired SendgridF sendgrid,
            @Autowired JWSSigner signer,
            @Autowired RefreshService refreshService,
            @Autowired UserInfoService userInfoService,
            @Autowired OauthScopes allowedScopes,
            @Autowired ReadmeF readme) {
        return new OtpService(otpRepository, templates, sendgrid, signer, refreshService, userInfoService,
                allowedScopes, readme);
    }

    @Bean
    public OtpController otpController(@Autowired OtpService otpService) {
        return new OtpController(otpService);
    }

    @Bean(name = "otpMustache")
    public MustacheF otpMustache() {
        MustacheF mustache = new MustacheF();
        mustache.load("templates", TEMPLATE_BODY_HTML, TEMPLATE_BODY_TXT, TEMPLATE_SUBJECT);
        return mustache;
    }
}
