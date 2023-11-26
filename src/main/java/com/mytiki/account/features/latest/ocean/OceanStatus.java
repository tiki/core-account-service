/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

public enum OceanStatus {
    PENDING("pending"),
    SUCCESS("success"),
    FAILED("failed");

    private final String string;

    OceanStatus(String string) {
        this.string = string;
    }

    public static OceanStatus fromString(String s) {
        for(OceanStatus status : values()){
            if( status.toString().equals(s)){
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return string;
    }
}
