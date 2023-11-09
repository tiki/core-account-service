/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.confirm;

public enum ConfirmAction {
    DELETE_USER("delete_user"),
    UPDATE_EMAIL("update_email");

    private final String value;

    ConfirmAction(String value) {
        this.value = value;
    }

    public static ConfirmAction find(String value){
        for(ConfirmAction action : values()){
            if( action.getValue().equals(value)){
                return action;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
