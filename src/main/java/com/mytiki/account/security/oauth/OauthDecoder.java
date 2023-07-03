package com.mytiki.account.security.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;
import java.util.function.Predicate;

public class OauthDecoder{
    public static String issuer;

    @Bean
    public JWTProcessor<SecurityContext> jwtProcessor(@Autowired JWKSet jwkSet) {
        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        ImmutableJWKSet<SecurityContext> immutableJWKSet = new ImmutableJWKSet<>(jwkSet);
        jwtProcessor.setJWSKeySelector(
                new JWSVerificationKeySelector<>(JWSAlgorithm.ES256, immutableJWKSet));
        return jwtProcessor;
    }

    @Bean
    public JwtDecoder jwtDecoder(
            @Autowired JWTProcessor<SecurityContext> jwtProcessor,
            @Value("${spring.security.oauth2.resourceserver.jwt.audiences}") List<String> audiences,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer) {
        OauthDecoder.issuer = issuer;
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

    public static void guardGroups(JwtAuthenticationToken token, String... expected){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<Set<String>> typeReference = new TypeReference<>(){};
            Set<String> claim = objectMapper.readValue(
                    token.getTokenAttributes().get("groups").toString(), typeReference);
            if(!claim.containsAll(Set.of(expected))){
                throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN)
                        .detail("Invalid groups claim")
                        .help("User does not belong to the group(s)")
                        .build();
            }
        } catch (JsonProcessingException e) {
            throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN)
                    .detail("Invalid groups claim")
                    .build();
        }
    }
}
