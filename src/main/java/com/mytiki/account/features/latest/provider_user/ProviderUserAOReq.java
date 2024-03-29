/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.provider_user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProviderUserAOReq {
    private String id;
    private String address;
    private String pubKey;
    private String signature;

    @JsonCreator
    public ProviderUserAOReq(
            @JsonProperty(required = true) String id,
            @JsonProperty(required = true) String address,
            @JsonProperty(required = true) String pubKey,
            @JsonProperty(required = true) String signature) {
        this.id = id;
        this.address = address;
        this.pubKey = pubKey;
        this.signature = signature;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
