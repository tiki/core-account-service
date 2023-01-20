/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest;

import com.mytiki.l0_auth.features.latest.app_info.AppInfoConfig;
import com.mytiki.l0_auth.features.latest.otp.OtpConfig;
import com.mytiki.l0_auth.features.latest.refresh.RefreshConfig;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoConfig;
import org.springframework.context.annotation.Import;

@Import({
        OtpConfig.class,
        RefreshConfig.class,
        UserInfoConfig.class,
        AppInfoConfig.class
})
public class FeaturesConfig {
}
