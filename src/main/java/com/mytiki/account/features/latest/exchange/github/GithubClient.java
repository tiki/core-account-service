/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.exchange.github;

import com.mytiki.account.features.latest.exchange.ExchangeClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GithubClient implements ExchangeClient {
    private final String secret;

    public GithubClient(String secret) {
        this.secret = secret;
    }

    @Override
    public String validate(String clientId, String token) {
        try {
            RestTemplate client = new RestTemplateBuilder()
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", secret);
            body.add("code", token);
            HttpEntity<MultiValueMap<String, String>> tokenReq = new HttpEntity<>(body, tokenHeaders);
            ResponseEntity<GithubAOToken> tokenRsp = client.exchange(
                    "https://github.com/login/oauth/access_token", HttpMethod.POST, tokenReq, GithubAOToken.class);
            if (tokenRsp.getStatusCode().is2xxSuccessful() &&
                    tokenRsp.getBody() != null &&
                    tokenRsp.getBody().getAccessToken() != null) {
                HttpHeaders userHeaders = new HttpHeaders();
                userHeaders.setBearerAuth(tokenRsp.getBody().getAccessToken());
                ResponseEntity<GithubAOEmail[]> userRsp = client.exchange("https://api.github.com/user/emails",
                        HttpMethod.GET, new HttpEntity<>(userHeaders), GithubAOEmail[].class);
                if (userRsp.getStatusCode().is2xxSuccessful() && userRsp.getBody() != null)
                    return Arrays
                            .stream(userRsp.getBody())
                            .filter(GithubAOEmail::getPrimary)
                            .findFirst()
                            .map(GithubAOEmail::getEmail)
                            .orElse(null);
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
}
