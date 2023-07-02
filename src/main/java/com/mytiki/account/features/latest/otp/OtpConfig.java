/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.otp;

import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.features.latest.user_info.UserInfoService;
import com.mytiki.account.security.OauthScopes;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.Mustache;
import com.mytiki.account.utilities.Sendgrid;
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
    public static final String PACKAGE_PATH = Constants.PACKAGE_FEATURES_LATEST_DOT_PATH + ".otp";
    public static final String TEMPLATE_BODY_HTML = "otp-body-html.mustache";
    public static final String TEMPLATE_BODY_TXT = "otp-body-txt.mustache";
    public static final String TEMPLATE_SUBJECT = "otp-subject.mustache";

    @Bean
    public OtpService otpService(
            @Autowired OtpRepository otpRepository,
            @Autowired @Qualifier("otpMustache") Mustache templates,
            @Autowired Sendgrid sendgrid,
            @Autowired JWSSigner jwsSigner,
            @Autowired RefreshService refreshService,
            @Autowired UserInfoService userInfoService,
            @Autowired OauthScopes allowedScopes,
            @Value("${com.mytiki.account.oauth.password.anonymous.scopes}") List<String> anonymousScopes) {
        return new OtpService(otpRepository, templates, sendgrid, jwsSigner, refreshService, userInfoService,
                allowedScopes, anonymousScopes);
    }

    @Bean
    public OtpController otpController(@Autowired OtpService otpService) {
        return new OtpController(otpService);
    }

    @Bean(name = "otpMustache")
    public Mustache otpMustache() {
        Mustache mustache = new Mustache();
        mustache.load("templates", TEMPLATE_BODY_HTML, TEMPLATE_BODY_TXT, TEMPLATE_SUBJECT);
        return mustache;
    }
}
