/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.security;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Order(value = Integer.MIN_VALUE)
@Import({
        OauthScopes.class,
        OauthInternal.class
})
@ControllerAdvice
public class OauthConfig {
    protected static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private List<OauthScope> scopes;
    private OauthInternal internalKeys;

    @Bean
    public List<OauthScope> getScopes() {
        return scopes;
    }

    public void setScopes(List<OauthScope> scopes) {
        this.scopes = scopes;
    }

    @Bean
    public HttpMessageConverter<OAuth2AccessTokenResponse> oAuth2AccessTokenResponseHttpMessageConverter() {
        return new OAuth2AccessTokenResponseHttpMessageConverter();
    }

    @Bean
    public HttpMessageConverter<OAuth2Error> oAuth2ErrorHttpMessageConverter() {
        return new OAuth2ErrorHttpMessageConverter();
    }

    @ExceptionHandler({OAuth2AuthorizationException.class})
    public ResponseEntity<OAuth2Error> handleException(OAuth2AuthorizationException e, HttpServletRequest request) {
        logger.error("Request: " + request.getRequestURI() + "caused {}", e);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (e.getError().getErrorCode().equals(OAuth2ErrorCodes.INVALID_CLIENT) ||
                e.getError().getErrorCode().equals(OAuth2ErrorCodes.INVALID_TOKEN))
            status = HttpStatus.UNAUTHORIZED;
        else if (e.getError().getErrorCode().equals(OAuth2ErrorCodes.INVALID_SCOPE))
            status = HttpStatus.FORBIDDEN;

        return ResponseEntity.status(status).body(e.getError());
    }
}
