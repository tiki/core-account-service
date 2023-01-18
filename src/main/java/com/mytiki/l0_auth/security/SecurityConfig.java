/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.l0_auth.features.latest.oauth.OauthController;
import com.mytiki.l0_auth.features.latest.otp.OtpController;
import com.mytiki.spring_rest_api.ApiConstants;
import com.mytiki.spring_rest_api.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
public class SecurityConfig {
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(@Autowired ObjectMapper objectMapper) {
        this.accessDeniedHandler = new AccessDeniedHandler(objectMapper);
        this.authenticationEntryPoint = new AuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .addFilter(new WebAsyncManagerIntegrationFilter())
                .servletApi().and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authenticationEntryPoint).and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .securityContext().and()
                .headers()
                .cacheControl().and()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity().and()
                .frameOptions().and()
                .xssProtection().and()
                .referrerPolicy().and()
                .permissionsPolicy().policy(SecurityConstants.FEATURE_POLICY).and()
                .contentSecurityPolicy(SecurityConstants.CONTENT_SECURITY_POLICY).and().and()
                .anonymous().and()
                .cors().configurationSource(SecurityConstants.corsConfigurationSource()).and()
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                        new AntPathRequestMatcher(OauthController.PATH_CONTROLLER + "/**"),
                        new AntPathRequestMatcher(OtpController.PATH_CONTROLLER + OtpController.PATH_ISSUE,
                                HttpMethod.POST.name())
                ).and()
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, ApiConstants.HEALTH_ROUTE).permitAll()
                .requestMatchers(HttpMethod.POST, OauthController.PATH_CONTROLLER + "/**").permitAll()
                .requestMatchers(HttpMethod.POST,
                        OtpController.PATH_CONTROLLER + OtpController.PATH_ISSUE).permitAll()
                .anyRequest().authenticated();
        return http.build();
    }
}
