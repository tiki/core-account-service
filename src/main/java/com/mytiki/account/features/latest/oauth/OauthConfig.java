/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.oauth;

import com.mytiki.account.features.latest.auth_code.AuthCodeService;
import com.mytiki.account.features.latest.provider_user.ProviderUserService;
import com.mytiki.account.features.latest.api_key.ApiKeyService;
import com.mytiki.account.features.latest.provider.ProviderService;
import com.mytiki.account.features.latest.exchange.ExchangeService;
import com.mytiki.account.features.latest.otp.OtpService;
import com.mytiki.account.features.latest.refresh.RefreshService;
import com.nimbusds.jose.JWSSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Order(value = Integer.MIN_VALUE)
@Import({
        OauthScopes.class,
        OauthInternalCreds.class,
        OauthExecHandler.class,
        OauthDecoder.class
})
@ControllerAdvice
public class OauthConfig {
    @Bean
    public HttpMessageConverter<OAuth2AccessTokenResponse> oAuth2AccessTokenResponseHttpMessageConverter() {
        return new OAuth2AccessTokenResponseHttpMessageConverter();
    }

    @Bean
    public HttpMessageConverter<OAuth2Error> oAuth2ErrorHttpMessageConverter() {
        return new OAuth2ErrorHttpMessageConverter();
    }

    @Bean
    public OauthInternal oauthInternal(
            @Autowired RefreshService refreshService,
            @Autowired JWSSigner signer,
            @Autowired OauthInternalCreds creds) {
        return new OauthInternal(refreshService, signer, creds);
    }

    @Bean
    public OauthController oauthController(
            @Autowired RefreshService refreshService,
            @Autowired ApiKeyService apiKeyService,
            @Autowired ExchangeService exchangeService,
            @Autowired ProviderUserService providerUserService,
            @Autowired ProviderService providerService,
            @Autowired AuthCodeService authCodeService,
            @Autowired OtpService otpService,
            @Autowired OauthScopes allowedScopes,
            @Autowired OauthInternal oauthInternal,
            @Autowired JwtDecoder decoder){
        return new OauthController(refreshService, apiKeyService,
                exchangeService, providerUserService, providerService, authCodeService,
                otpService, allowedScopes, oauthInternal, decoder);
    }
}
