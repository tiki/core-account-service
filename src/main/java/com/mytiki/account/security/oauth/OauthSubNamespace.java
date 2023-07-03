package com.mytiki.account.security.oauth;

import java.util.HashMap;
import java.util.Map;

public enum OauthSubNamespace {
    USER("user"),
    APP("app"),
    ORG("org");

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
