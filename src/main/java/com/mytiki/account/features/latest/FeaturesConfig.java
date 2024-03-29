/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest;

import com.mytiki.account.features.latest.auth_code.AuthCodeConfig;
import com.mytiki.account.features.latest.cleanroom.CleanroomConfig;
import com.mytiki.account.features.latest.event.EventConfig;
import com.mytiki.account.features.latest.provider_user.ProviderUserConfig;
import com.mytiki.account.features.latest.api_key.ApiKeyConfig;
import com.mytiki.account.features.latest.provider.ProviderConfig;
import com.mytiki.account.features.latest.confirm.ConfirmConfig;
import com.mytiki.account.features.latest.exchange.ExchangeConfig;
import com.mytiki.account.features.latest.jwks.JwksConfig;
import com.mytiki.account.features.latest.oauth.OauthConfig;
import com.mytiki.account.features.latest.org.OrgConfig;
import com.mytiki.account.features.latest.otp.OtpConfig;
import com.mytiki.account.features.latest.readme.ReadmeConfig;
import com.mytiki.account.features.latest.refresh.RefreshConfig;
import com.mytiki.account.features.latest.profile.ProfileConfig;
import com.mytiki.account.features.latest.subscription.SubscriptionConfig;
import org.springframework.context.annotation.Import;

@Import({
        OtpConfig.class,
        RefreshConfig.class,
        ProfileConfig.class,
        ProviderConfig.class,
        ApiKeyConfig.class,
        OrgConfig.class,
        ExchangeConfig.class,
        ProviderUserConfig.class,
        JwksConfig.class,
        ConfirmConfig.class,
        OauthConfig.class,
        AuthCodeConfig.class,
        ReadmeConfig.class,
        EventConfig.class,
        CleanroomConfig.class,
        SubscriptionConfig.class
})
public class FeaturesConfig {
}
