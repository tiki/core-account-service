/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.security.oauth;

public class OauthSub {
    private String id;
    private OauthSubNamespace namespace;

    public OauthSub() {}

    public OauthSub(String namespace, String id) {
        this.id = id;
        this.namespace = OauthSubNamespace.from(namespace);
    }

    public OauthSub(OauthSubNamespace namespace, String id) {
        this.id = id;
        this.namespace = namespace;
    }

    public OauthSub(String sub) {
        if(sub != null) {
            String[] split = sub.split(":", 1);
            if (split.length != 2) {
                id = sub;
            } else {
                namespace = OauthSubNamespace.from(split[0]);
                id = split[1];
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OauthSubNamespace getNamespace() {
        return namespace;
    }

    public void setNamespace(OauthSubNamespace namespace) {
        this.namespace = namespace;
    }

    public boolean isUser() {
        return namespace.equals(OauthSubNamespace.USER);
    }

    public boolean isApp() {
        return namespace.equals(OauthSubNamespace.APP);
    }

    public boolean isInternal() {
        return namespace.equals(OauthSubNamespace.INTERNAL);
    }

    public boolean isAddress() {
        return namespace.equals(OauthSubNamespace.ADDRESS);
    }

    @Override
    public String toString() {
        return namespace != null ? namespace + ":" + id : id;
    }
}
