/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities;


import com.amazonaws.xray.spring.aop.BaseAbstractXRayInterceptor;
import com.mytiki.account.utilities.facade.SendgridF;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import jakarta.servlet.Filter;
import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.strategy.jakarta.SegmentNamingStrategy;

@Import(PublicResolver.class)
public class UtilitiesConfig {
    @Bean
    public SendgridF sendgridFacade(@Value("${com.mytiki.account.sendgrid.apikey}") String sendgridApiKey) {
        return new SendgridF(sendgridApiKey);
    }

    @Bean
    public Filter TracingFilter() {
        return new AWSXRayServletFilter(SegmentNamingStrategy.dynamic("CoreAccountService"));
    }

    @Bean
    public BaseAbstractXRayInterceptor xRayInterceptor() {
        return new XRayInspector();
    }
}
