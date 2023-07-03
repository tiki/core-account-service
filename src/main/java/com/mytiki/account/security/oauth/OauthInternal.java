/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.security.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(value = "com.mytiki.account.oauth.client-credentials.internal")
public class OauthInternal {
    private List<String> scopes;
    private String json;

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public Map<String, String> getKeys(){
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            TypeReference<Map<String,String>> type = new TypeReference<Map<String,String>>() {};
            Map<String, String> keys = objectMapper.readValue(this.json, type);
            return keys;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
