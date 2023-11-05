/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PublicResolver implements WebMvcConfigurer {
    private static String PAGE_SUFFIX = ".html";
    public static String PAGES = "/pages";
    public static String ASSETS = "/assets";
    public static String PAGE_LOGIN = PAGES + "/login";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController(PAGE_LOGIN).setViewName(PAGE_LOGIN + PAGE_SUFFIX);
    }
}
