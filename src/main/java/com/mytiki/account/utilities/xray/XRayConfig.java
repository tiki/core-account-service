/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.xray;

import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

@Import(XRayInspector.class)
@Configuration
public class XRayConfig {

    @Bean
    public Filter TracingFilter() {
        return new XRayFilter(List.of("/health"));
    }
}
