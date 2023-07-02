package com.mytiki.account.features.latest.addr_reg;

import com.mytiki.account.features.latest.api_key.ApiKeyConfig;
import com.mytiki.account.utilities.Constants;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(AddrRegConfig.PACKAGE_PATH)
@EntityScan(AddrRegConfig.PACKAGE_PATH)
public class AddrRegConfig {
    public static final String PACKAGE_PATH = Constants.PACKAGE_FEATURES_LATEST_DOT_PATH + ".addr_reg";

    @Bean
    AddrRegService addrRegService() {
        return new AddrRegService();
    }

    @Bean
    AddrRegController addrRegController() {
        return new AddrRegController();
    }
}
