/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.security;

import java.util.List;

public class OauthScope {
    private List<String> aud;
    private List<String> scp;

    public List<String> getAud() {
        return aud;
    }

    public void setAud(List<String> aud) {
        this.aud = aud;
    }

    public List<String> getScp() {
        return scp;
    }

    public void setScp(List<String> scp) {
        this.scp = scp;
    }
}
