/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.event.ao;

public class EventAOErrorRsp extends EventAOBase {
    private String message;
    private String cause;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    @Override
    public String toString() {
        return "EventAOErrorRsp{" +
                "message='" + message + '\'' +
                ", cause='" + cause + '\'' +
                '}';
    }
}
