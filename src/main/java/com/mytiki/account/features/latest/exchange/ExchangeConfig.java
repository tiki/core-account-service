/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.exchange;

import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.features.latest.user_info.UserInfoService;
import com.mytiki.account.security.oauth.OauthScopes;
import com.mytiki.account.utilities.facade.ReadmeF;
import com.nimbusds.jose.JWSSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class ExchangeConfig {
    @Bean
    public ExchangeService exchangeService(
            @Autowired UserInfoService userInfoService,
            @Autowired RefreshService refreshService,
            @Autowired JWSSigner signer,
            @Autowired OauthScopes allowedScopes,
            @Autowired ReadmeF readme) {
        return new ExchangeService(userInfoService, refreshService, signer, allowedScopes, readme);
    }

    @Bean
    public ExchangeController exchangeController(@Autowired ExchangeService service) {
        return new ExchangeController(service);
    }
}
