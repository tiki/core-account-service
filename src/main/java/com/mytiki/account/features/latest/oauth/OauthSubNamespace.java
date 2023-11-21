/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.oauth;

import java.util.HashMap;
import java.util.Map;

public enum OauthSubNamespace {
    USER("user"),
    PROVIDER("provider"),
    INTERNAL("internal"),
    ADDRESS("addr");

    public final String namespace;
    private static final Map<String, OauthSubNamespace> cache = new HashMap<>();

    static {
        for (OauthSubNamespace e: values()) {
            cache.put(e.namespace, e);
        }
    }

    OauthSubNamespace(String namespace) {
        this.namespace = namespace;
    }

    public static OauthSubNamespace from(String namespace) {
        return cache.get(namespace);
    }

    @Override
    public String toString() {
        return namespace;
    }
}
