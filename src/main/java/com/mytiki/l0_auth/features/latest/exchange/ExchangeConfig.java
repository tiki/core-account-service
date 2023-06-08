/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.exchange;

import com.mytiki.l0_auth.features.latest.refresh.RefreshService;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoService;
import com.mytiki.l0_auth.security.OauthScopes;
import com.nimbusds.jose.JWSSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class ExchangeConfig {
    @Bean
    public ExchangeService exchangeService(
            @Autowired UserInfoService userInfoService,
            @Autowired RefreshService refreshService,
            @Autowired JWSSigner signer,
            @Autowired OauthScopes allowedScopes) {
        return new ExchangeService(userInfoService, refreshService, signer, allowedScopes);
    }

    @Bean
    public ExchangeController exchangeController(@Autowired ExchangeService service) {
        return new ExchangeController(service);
    }
}
