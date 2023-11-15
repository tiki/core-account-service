/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest;

import com.mytiki.account.features.latest.addr_reg.AddrRegConfig;
import com.mytiki.account.features.latest.api_key.ApiKeyConfig;
import com.mytiki.account.features.latest.app_info.AppInfoConfig;
import com.mytiki.account.features.latest.confirm.ConfirmConfig;
import com.mytiki.account.features.latest.exchange.ExchangeConfig;
import com.mytiki.account.security.jwks.JwksConfig;
import com.mytiki.account.features.latest.org_info.OrgInfoConfig;
import com.mytiki.account.features.latest.otp.OtpConfig;
import com.mytiki.account.features.latest.refresh.RefreshConfig;
import com.mytiki.account.features.latest.user_info.UserInfoConfig;
import org.springframework.context.annotation.Import;

@Import({
        OtpConfig.class,
        RefreshConfig.class,
        UserInfoConfig.class,
        AppInfoConfig.class,
        ApiKeyConfig.class,
        OrgInfoConfig.class,
        ExchangeConfig.class,
        AddrRegConfig.class,
        JwksConfig.class,
        ConfirmConfig.class
})
public class FeaturesConfig {
}
