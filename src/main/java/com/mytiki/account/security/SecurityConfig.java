/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.features.latest.otp.OtpController;
import com.mytiki.account.features.latest.stripe.StripeController;
import com.mytiki.spring_rest_api.ApiConstants;
import com.mytiki.spring_rest_api.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Import({
        OauthConfig.class,
        JWTConfig.class,
})
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final JwtDecoder jwtDecoder;

    public SecurityConfig(
            @Autowired ObjectMapper objectMapper,
            @Autowired JwtDecoder jwtDecoder) {
        this.accessDeniedHandler = new AccessDeniedHandler(objectMapper);
        this.authenticationEntryPoint = new AuthenticationEntryPoint(objectMapper);
        this.jwtDecoder = jwtDecoder;
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
                .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                        new AntPathRequestMatcher(ApiConstants.API_LATEST_ROUTE + Constants.OAUTH_TOKEN_PATH,
                                HttpMethod.POST.name()),
                        new AntPathRequestMatcher(ApiConstants.API_LATEST_ROUTE + OtpController.PATH_ISSUE,
                                HttpMethod.POST.name()),
                        new AntPathRequestMatcher(StripeController.PATH_CONTROLLER + "/*",
                                HttpMethod.POST.name())
                ).and()
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET,
                        ApiConstants.HEALTH_ROUTE,
                        Constants.API_DOCS_PATH,
                        JWKSController.PATH
                ).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                .permitAll()
                .requestMatchers(HttpMethod.POST, StripeController.PATH_CONTROLLER + "/*")
                .permitAll()
                .requestMatchers(HttpMethod.POST,
                        ApiConstants.API_LATEST_ROUTE + Constants.OAUTH_TOKEN_PATH,
                        ApiConstants.API_LATEST_ROUTE + OtpController.PATH_ISSUE
                ).permitAll()
                .anyRequest().authenticated().and()
                .oauth2ResourceServer()
                .jwt().decoder(jwtDecoder).and()
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authenticationEntryPoint);
        return http.build();
    }
}
