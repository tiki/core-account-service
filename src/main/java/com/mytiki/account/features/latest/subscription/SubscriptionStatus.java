/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

public enum SubscriptionStatus {
    ESTIMATE("estimate"),
    SUBSCRIBED("subscribed"),
    STOPPED("stopped");

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
