/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.event.type;

public enum EventType {
    CREATE_CLEANROOM("cr_create"),
    ESTIMATE_SUBSCRIPTION("sub_estimate"),
    PURCHASE_SUBSCRIPTION("sub_purchase");

    private final String string;

    EventType(String string) {
        this.string = string;
    }

    public static EventType fromString(String s) {
        for(EventType type : values()){
            if( type.toString().equals(s)){
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return string;
    }
}
