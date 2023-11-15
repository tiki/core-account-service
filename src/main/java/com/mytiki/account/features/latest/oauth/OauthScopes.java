/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties(value = "com.mytiki.account.oauth")
public class OauthScopes {
    private Map<String,OauthScope> scopes;
    private List<String> aud;
    private List<String> scp;

    public Map<String, OauthScope> getScopes() {
        return scopes;
    }

    public List<String> getAud() {
        return aud;
    }

    public List<String> getScp() {
        return scp;
    }

    public void setScopes(Map<String, OauthScope> scopes) {
        this.scopes = scopes;
        Set<String> aud = new HashSet<>();
        Set<String> scp = new HashSet<>();
        scopes.values().forEach(scope -> {
            if(scope.getAud() != null) aud.addAll(scope.getAud());
            if(scope.getScp() != null) scp.addAll(scope.getScp());
        });
        this.aud = aud.stream().toList();
        this.scp = scp.stream().toList();
    }

    public OauthScopes filter(String criteria) {
        List<String> list = criteria == null ?
                new ArrayList<>() :
                Arrays.stream(criteria.split(" ")).toList();
        return filter(list);
    }

    public OauthScopes filter(List<String> criteria) {
        OauthScopes oauthScopes = new OauthScopes();
        Map<String,OauthScope> filtered = scopes
                .entrySet()
                .stream()
                .filter(entry -> criteria.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        oauthScopes.setScopes(filtered);
        return oauthScopes;
    }

    public static boolean hasScope(JwtAuthenticationToken token, String scope){
        return token.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("SCOPE_" + scope));
    }
}
