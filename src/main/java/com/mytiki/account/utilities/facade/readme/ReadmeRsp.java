/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.facade.readme;

import java.util.List;
import java.util.Map;

public class ReadmeRsp {
    private String name;
    private String email;
    private List<Map<String, String>> keys;

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

    public List<Map<String, String>> getKeys() {
        return keys;
    }

    public void setKeys(List<Map<String, String>> keys) {
        this.keys = keys;
    }
}
