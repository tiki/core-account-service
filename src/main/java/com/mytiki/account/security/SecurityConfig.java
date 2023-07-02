/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.security;

import com.mytiki.account.security.oauth.OauthConfig;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Import({
        SecurityFilter.class,
        OauthConfig.class,
})
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {}
