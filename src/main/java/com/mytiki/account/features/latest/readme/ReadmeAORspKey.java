/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.readme;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ReadmeAORspKey {
    private String apiKey;
    private String key;
    private String name;
    private ReadmeAORspParams parameters;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ReadmeAORspParams getParameters() {
        return parameters;
    }

    public void setParameters(ReadmeAORspParams parameters) {
        this.parameters = parameters;
    }
}
