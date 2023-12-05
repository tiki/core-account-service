/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.readme;

import java.util.List;
import java.util.Map;

public class ReadmeAORsp {
    private String name;
    private String email;
    private Integer version;
    private String billing;
    private List<ReadmeAORspKey> keys;
    private ReadmeAORspParams parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<ReadmeAORspKey> getKeys() {
        return keys;
    }

    public void setKeys(List<ReadmeAORspKey> keys) {
        this.keys = keys;
    }

    public ReadmeAORspParams getParameters() {
        return parameters;
    }

    public void setParameters(ReadmeAORspParams parameters) {
        this.parameters = parameters;
    }

    public String getBilling() {
        return billing;
    }

    public void setBilling(String billing) {
        this.billing = billing;
    }
}
