package com.mytiki.account.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.utilities.Constants;
import com.mytiki.spring_rest_api.ApiConstants;
import com.mytiki.spring_rest_api.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

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
                       .permissionsPolicy((pp) -> pp.policy(SecurityConstants.FEATURE_POLICY)))
               .anonymous(Customizer.withDefaults())
               .cors((cors) -> cors
                       .configurationSource(SecurityConstants.corsConfigurationSource()))
               .csrf((csrf) -> csrf
                       .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                       .ignoringRequestMatchers(ApiConstants.API_LATEST_ROUTE + Constants.AUTH_PATH + "/**"))
               .authorizeHttpRequests((req) -> req
                       .requestMatchers(HttpMethod.GET, ApiConstants.HEALTH_ROUTE).permitAll()
                       .requestMatchers(HttpMethod.GET, Constants.API_DOCS_PATH).permitAll()
                       .requestMatchers(HttpMethod.GET, Constants.WELL_KNOWN_PATH + "/**").permitAll()
                       .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                       .requestMatchers(HttpMethod.POST, ApiConstants.API_LATEST_ROUTE + Constants.AUTH_PATH + "/**").permitAll()
                       .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.decoder(jwtDecoder))
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint))
                .build();
    }
}
