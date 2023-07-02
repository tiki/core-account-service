package com.mytiki.account.features.latest.addr_reg;

import com.mytiki.account.features.latest.app_info.AppInfoService;
import com.mytiki.account.utilities.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(AddrRegConfig.PACKAGE_PATH)
@EntityScan(AddrRegConfig.PACKAGE_PATH)
public class AddrRegConfig {
    public static final String PACKAGE_PATH = Constants.PACKAGE_FEATURES_LATEST_DOT_PATH + ".addr_reg";

    @Bean
    AddrRegService addrRegService(
            @Autowired AddrRegRepository repository,
            @Autowired AppInfoService appInfoService) {
        return new AddrRegService(repository, appInfoService);
    }

    @Bean
    AddrRegController addrRegController(@Autowired AddrRegService service) {
        return new AddrRegController(service);
    }
}
