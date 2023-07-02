/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.security.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties(value = "com.mytiki.account.oauth")
public class OauthScopes {
    private Map<String,OauthScope> scopes;

    public Map<String, OauthScope> getScopes() {
        return scopes;
    }

    public void setScopes(Map<String, OauthScope> scopes) {
        this.scopes = scopes;
    }

    public Map<String, OauthScope> parse(String req){
        return req == null ?
                new HashMap<>() :
                Arrays.stream(req.split(" "))
                        .filter(scopes::containsKey)
                        .collect(Collectors.toMap(e -> e, scopes::get));
    }

    public Map<String, OauthScope> filter(Map<String, OauthScope> scopes, List<String> criteria){
        return scopes.entrySet()
                .stream()
                .filter(entry -> criteria.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<String>[] getAudAndScp(Map<String, OauthScope> scopes){
        Set<String> aud = new HashSet<>();
        Set<String> scp = new HashSet<>();
        scopes.values().forEach(scope -> {
            if(scope.getAud() != null)
                aud.addAll(scope.getAud());
            if(scope.getScp() != null)
                scp.addAll(scope.getScp());
        });
        List<String>[] rsp = new List[2];
        rsp[0] = aud.stream().toList();
        rsp[1] = scp.stream().toList();
        return rsp;
    }

    public static boolean hasScope(JwtAuthenticationToken token, String scope){
        return token.getAuthorities().contains(new SimpleGrantedAuthority("SCOPE_" + scope));
    }
}
