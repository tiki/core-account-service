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
    public static String PAGE_OTP = PAGES + "/otp";
    public static String PAGE_LOGOUT = PAGES + "/logout";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController(PAGE_LOGIN).setViewName(PAGE_LOGIN + PAGE_SUFFIX);
        registry.addViewController(PAGE_OTP).setViewName(PAGE_OTP + PAGE_SUFFIX);
        registry.addViewController(PAGE_LOGOUT).setViewName(PAGE_LOGOUT + PAGE_SUFFIX);
    }
}
