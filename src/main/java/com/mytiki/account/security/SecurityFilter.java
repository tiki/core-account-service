/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.otp.OtpController;
import com.mytiki.account.features.latest.jwks.JwksController;
import com.mytiki.account.features.latest.readme.ReadmeController;
import com.mytiki.account.health.HealthController;
import com.mytiki.account.features.latest.oauth.OauthController;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.PublicResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

public class SecurityFilter {
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final JwtDecoder jwtDecoder;

    public SecurityFilter(
            @Autowired ObjectMapper objectMapper,
            @Autowired JwtDecoder jwtDecoder) {
        this.accessDeniedHandler = new AccessDeniedHandler(objectMapper);
        this.authenticationEntryPoint = new AuthenticationEntryPoint(objectMapper);
        this.jwtDecoder = jwtDecoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       return http
               .addFilter(new WebAsyncManagerIntegrationFilter())
               .servletApi(Customizer.withDefaults())
               .exceptionHandling((handling) -> handling
                       .accessDeniedHandler(accessDeniedHandler)
                       .authenticationEntryPoint(authenticationEntryPoint))
               .sessionManagement((session) -> session
                       .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .securityContext(Customizer.withDefaults())
               .headers((headers) -> headers
                       .cacheControl(Customizer.withDefaults())
                       .contentTypeOptions(Customizer.withDefaults())
                       .httpStrictTransportSecurity(Customizer.withDefaults())
                       .frameOptions(Customizer.withDefaults())
                       .xssProtection(Customizer.withDefaults())
                       .referrerPolicy(Customizer.withDefaults())
                       .permissionsPolicy((pp) -> pp.policy(
                               "accelerometer" + " 'none'" + "ambient-light-sensor" + " 'none'" +
                               "autoplay" + " 'none'" + "battery" + " 'none'" + "camera" + " 'none'" + "display-capture" + " 'none'" +
                               "document-domain" + " 'none'" + "encrypted-media" + " 'none'" + "execution-while-not-rendered" + " 'none'" +
                               "execution-while-out-of-viewport" + " 'none'" + "fullscreen" + " 'none'" + "geolocation" + " 'none'" +
                               "gyroscope" + " 'none'" + "layout-animations" + " 'none'" + "legacy-image-formats" + " 'none'" +
                               "magnetometer" + " 'none'" + "microphone" + " 'none'" + "midi" + " 'none'" + "navigation-override" + " 'none'" +
                               "oversized-images" + " 'none'" + "payment" + " 'none'" + "picture-in-picture" + " 'none'" + "publickey-credentials-get" + " 'none'" +
                               "sync-xhr" + " 'none'" + "usb" + " 'none'" + "vr wake-lock" + " 'none'" + "xr-spatial-tracking" + " 'none'")))
               .anonymous(Customizer.withDefaults())
               .cors((cors) -> {
                   CorsConfiguration configuration = new CorsConfiguration();
                   configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
                   configuration.setAllowedMethods(
                           Arrays.asList(
                                   HttpMethod.OPTIONS.name(),
                                   HttpMethod.GET.name(),
                                   HttpMethod.PUT.name(),
                                   HttpMethod.POST.name(),
                                   HttpMethod.DELETE.name()));
                   configuration.setAllowedHeaders(
                           Arrays.asList(
                                   HttpHeaders.CONTENT_TYPE,
                                   HttpHeaders.AUTHORIZATION,
                                   HttpHeaders.ACCEPT));
                   configuration.setAllowCredentials(true);
                   UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                   source.registerCorsConfiguration("/**", configuration);
                   cors.configurationSource(source);
               })
               .csrf(AbstractHttpConfigurer::disable)
               .authorizeHttpRequests((req) -> req
                       .requestMatchers(HttpMethod.GET, HealthController.ROUTE).permitAll()
                       .requestMatchers(HttpMethod.GET, Constants.API_DOCS_ROUTE).permitAll()
                       .requestMatchers(HttpMethod.GET, JwksController.ROUTE).permitAll()
                       .requestMatchers(HttpMethod.GET, PublicResolver.PAGES + "/**").permitAll()
                       .requestMatchers(HttpMethod.GET, PublicResolver.ASSETS + "/**").permitAll()
                       .requestMatchers(HttpMethod.POST, ReadmeController.ROUTE).permitAll()
                       .requestMatchers(HttpMethod.POST, OauthController.ROUTE + "/token").permitAll()
                       .requestMatchers(HttpMethod.POST, OauthController.ROUTE + "/revoke").permitAll()
                       .requestMatchers(HttpMethod.POST, OtpController.ROUTE).permitAll()
                       .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                       .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.decoder(jwtDecoder))
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint))
                .build();
    }
}
