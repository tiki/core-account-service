/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.jwks;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@XRayEnabled
public class JwksService {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final JwksRepository repository;
    private final RestTemplate client;
    private final int cacheSeconds;

    public JwksService(
            JwksRepository repository,
            RestTemplate client,
            int cacheSeconds) {
        this.repository = repository;
        this.client = client;
        this.cacheSeconds = cacheSeconds;
    }

    public JwksDO cache(String endpoint, Boolean verifySub) throws URISyntaxException {
        URI endpointUri = new URI(endpoint);
        Optional<JwksDO> found = repository.getByEndpoint(endpointUri);
        if(found.isEmpty()){
            JwksDO jwks = new JwksDO();
            jwks.setEndpoint(endpointUri);
            jwks.setVerifySub(verifySub);
            ZonedDateTime now = ZonedDateTime.now();
            jwks.setCreated(now);
            jwks.setModified(now);
            return repository.save(jwks);
        }else if(found.get().getVerifySub() != verifySub) {
            JwksDO update = found.get();
            update.setVerifySub(verifySub);
            update.setModified(ZonedDateTime.now());
            return repository.save(update);
        }else return found.get();
    }

    public Optional<JwksDO> get(URI endpoint) {
        Optional<JwksDO> saved = repository.getByEndpoint(endpoint);
        if(saved.isEmpty()) return Optional.empty();
        JwksDO jwks = saved.get();
        if(
                jwks.getKeySet() == null ||
                ZonedDateTime.now().toEpochSecond() -
                jwks.getModified().toEpochSecond() > cacheSeconds)
        {
            JWKSet keySet = fetchKeySet(endpoint);
            if(keySet != null) {
                ZonedDateTime now = ZonedDateTime.now();
                jwks.setKeySet(keySet);
                jwks.setModified(now);
                return Optional.of(repository.save(jwks));
            }else return Optional.empty();
        }else return saved;
    }

    public void guard(URI endpoint, String token){
        guard(endpoint, token, null);
    }

    public void guard(URI endpoint, String token, String sub){
        Optional<JwksDO> found = get(endpoint);
        if(found.isEmpty())
            throw new ErrorBuilder(HttpStatus.FORBIDDEN)
                .detail("JWKS Endpoint Not Found")
                .exception();
        try {
            if(found.get().getKeySet() == null)
                throw new ErrorBuilder(HttpStatus.FORBIDDEN)
                        .detail("JWKS KeySet Not Found")
                        .exception();

            JwtDecoder decoder = decoder(found.get().getKeySet());
            Jwt jwt = decoder.decode(token);
            if(found.get().getVerifySub() && !jwt.getSubject().equals(sub)){
                throw new ErrorBuilder(HttpStatus.FORBIDDEN)
                        .detail("Invalid claim: sub")
                        .exception();
            }
        } catch (JwtException e){
            throw new ErrorBuilder(HttpStatus.UNAUTHORIZED)
                    .message("Invalid token")
                    .exception();
        }
    }

    private JWKSet fetchKeySet(URI endpoint){
        try {
            ResponseEntity<String> response = client.getForEntity(endpoint, String.class);
            if(response.getStatusCode().is2xxSuccessful()){
                JWKSet jwkSet = JWKSet.parse(response.getBody());
                return new JWKSet(jwkSet.getKeys()
                        .stream()
                        .filter(jwk -> jwk.getKeyUse().equals(KeyUse.SIGNATURE))
                        .filter(jwk -> jwk.getKeyType().equals(KeyType.EC) || jwk.getKeyType().equals(KeyType.RSA))
                        .toList());
            }
        } catch (Exception e) {
            logger.error("Failed to fetch endpoint, skipping.", e);
        }
        return null;
    }

    private JwtDecoder decoder(JWKSet jwkSet){
        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(
                jwkSet.getKeys()
                        .stream()
                        .map(jwt -> JWSAlgorithm.parse(jwt.getAlgorithm().getName()))
                        .collect(Collectors.toSet()),
                new ImmutableJWKSet<>(jwkSet)));
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(jwtProcessor);
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator());
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return decoder;
    }
}
