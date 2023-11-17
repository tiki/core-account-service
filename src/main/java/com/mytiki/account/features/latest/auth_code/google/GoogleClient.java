/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.auth_code.google;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.mytiki.account.features.latest.auth_code.AuthCodeClient;
import com.mytiki.account.features.latest.auth_code.github.GithubAOEmail;
import com.mytiki.account.features.latest.auth_code.github.GithubAOToken;
import com.mytiki.account.features.latest.exchange.ExchangeClient;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;

@XRayEnabled
public class GoogleClient implements AuthCodeClient {
    private final String id;
    private final String secret;
    private final JwtDecoder decoder;

    public GoogleClient(String id, String secret) {
        this.id = id;
        this.secret = secret;
        try {
            DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            URI remoteKeys = new URI("https://accounts.google.com/.well-known/openid-configuration");
            RemoteJWKSet<SecurityContext> jwkSet = new RemoteJWKSet<>(remoteKeys.toURL());
            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSet));
            decoder = new NimbusJwtDecoder(jwtProcessor);
        }catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String exchange(String code) {
        try {
            RestTemplate client = new RestTemplateBuilder()
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", id);
            body.add("client_secret", secret);
            body.add("code", code);
            body.add("grant_type", "authorization_code");
            body.add("redirect_uri", "https://account.mytiki.com/pages/code/google.html");
            HttpEntity<MultiValueMap<String, String>> tokenReq = new HttpEntity<>(body, tokenHeaders);
            ResponseEntity<GoogleAOToken> tokenRsp = client.exchange(
                    "https://oauth2.googleapis.com/token", HttpMethod.POST, tokenReq, GoogleAOToken.class);
            if (tokenRsp.getStatusCode().is2xxSuccessful() &&
                    tokenRsp.getBody() != null &&
                    tokenRsp.getBody().getIdToken() != null) {
                Jwt jwt = decoder.decode(tokenRsp.getBody().getIdToken());
                return jwt.getClaim("email");
            }
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.ACCESS_DENIED),
                    "client_id and/or subject_token_type are invalid");
        }catch (Exception e){
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "Issue with token exchange",
                    null
            ), e);
        }
    }

    @Override
    public String clientId() {
        return id;
    }
}
