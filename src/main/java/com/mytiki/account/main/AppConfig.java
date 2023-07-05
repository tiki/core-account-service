/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.main;

import com.mytiki.account.features.latest.FeaturesConfig;
import com.mytiki.account.health.HealthConfig;
import com.mytiki.account.security.SecurityConfig;
import com.mytiki.account.utilities.UtilitiesConfig;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.security.Security;
import java.util.Collections;
import java.util.TimeZone;

@Import({
        AppHandler.class,
        SecurityConfig.class,
        HealthConfig.class,
        UtilitiesConfig.class,
        FeaturesConfig.class
})
@EnableConfigurationProperties
public class AppConfig {
    @PostConstruct
    void starter() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    public OpenAPI openAPI(@Value("${springdoc.version}") String appVersion) {
        return new OpenAPI()
                .info(new Info()
                        .title("TIKI Account")
                        .description("TIKI's account management service —configure and provision access to the data platform")
                        .version(appVersion)
                        .license(new License()
                                .name("MIT")
                                .url("https://github.com/tiki/tiki-account/blob/main/LICENSE")))
                .servers(Collections.singletonList(new Server()
                        .url("https://mytiki.com/api/account")))
                .components(new Components()
                        .addSecuritySchemes("oauth", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .flows(new OAuthFlows()
                                        .password(new OAuthFlow()
                                                .tokenUrl("https://mytiki.com/api/account/oauth/token")
                                                .refreshUrl("https://mytiki.com/api/account/oauth/token")
                                                .scopes(new Scopes().addString("auth","this service")))))); //TODO fix scopes
    }
}
