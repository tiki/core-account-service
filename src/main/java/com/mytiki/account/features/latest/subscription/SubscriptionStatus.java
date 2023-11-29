/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import com.mytiki.account.features.latest.ocean.OceanStatus;

public enum SubscriptionStatus {
    ESTIMATE("estimate"),
    INITIALIZING("initializing"),
    UPDATING("updating"),
    SUBSCRIBED("subscribed"),
    STOPPED("stopped"),
    ERROR("error");

    private final String string;

    SubscriptionStatus(String string) {
        this.string = string;
    }

    public static SubscriptionStatus fromString(String s) {
        for(SubscriptionStatus status : values()){
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
