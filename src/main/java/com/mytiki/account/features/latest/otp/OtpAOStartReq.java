/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.otp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OtpAOStartReq {
    private String email;

    private boolean notAnonymous;

    @JsonCreator
    public OtpAOStartReq(@JsonProperty(required = true) String email, boolean notAnonymous) {
        this.email = email;
        this.notAnonymous = notAnonymous;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isNotAnonymous() {
        return notAnonymous;
    }

    public void setNotAnonymous(boolean notAnonymous) {
        this.notAnonymous = notAnonymous;
    }
}
