package com.mytiki.account.security.oauth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class OauthDecoder{
    public static String issuer;

    @Bean
    public JwtDecoder jwtDecoder(
            @Autowired JWKSet jwkSet,
            @Value("${spring.security.oauth2.resourceserver.jwt.audiences}") List<String> audiences,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer) {
        OauthDecoder.issuer = issuer;
        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        ImmutableJWKSet<SecurityContext> immutableJWKSet = new ImmutableJWKSet<>(jwkSet);
        jwtProcessor.setJWSKeySelector(
                new JWSVerificationKeySelector<>(JWSAlgorithm.ES256, immutableJWKSet));
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(jwtProcessor);
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator());
        validators.add(new JwtIssuerValidator(issuer));
        validators.add(new JwtClaimValidator<>(JwtClaimNames.IAT, Objects::nonNull));
        Predicate<List<String>> audienceTest = (audience) -> (audience != null)
                && new HashSet<>(audience).containsAll(audiences);
        validators.add(new JwtClaimValidator<>(JwtClaimNames.AUD, audienceTest));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return decoder;
    }
}
