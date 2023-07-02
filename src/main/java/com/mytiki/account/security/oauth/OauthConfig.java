/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.security.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Order(value = Integer.MIN_VALUE)
@Import({
        OauthScopes.class,
        OauthInternal.class,
        OauthExecHandler.class,
        OauthDecoder.class
})
@ControllerAdvice
public class OauthConfig {
    public static String issuer;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    public void setIssuer(String issuer){
        OauthConfig.issuer = issuer;
    }

    @Bean
    public HttpMessageConverter<OAuth2AccessTokenResponse> oAuth2AccessTokenResponseHttpMessageConverter() {
        return new OAuth2AccessTokenResponseHttpMessageConverter();
    }

    @Bean
    public HttpMessageConverter<OAuth2Error> oAuth2ErrorHttpMessageConverter() {
        return new OAuth2ErrorHttpMessageConverter();
    }
}
