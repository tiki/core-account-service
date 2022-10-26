/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.otp;

import com.mytiki.l0_auth.utilities.Constants;
import com.mytiki.l0_auth.utilities.Mustache;
import com.mytiki.l0_auth.utilities.Sendgrid;
import com.nimbusds.jose.JWSSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

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
            @Autowired JWSSigner jwsSigner) {
        return new OtpService(otpRepository, templates, sendgrid, jwsSigner);
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
