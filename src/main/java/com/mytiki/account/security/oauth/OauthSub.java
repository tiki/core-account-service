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
            String[] split = sub.split(":");
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

    @Override
    public String toString() {
        return namespace != null ? namespace + ":" + id : id;
    }
}
