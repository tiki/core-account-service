/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

public enum OceanType {
    COUNT("count"),
    SAMPLE("sample"),
    CREATE_TABLE("create_table"),
    UPDATE_TABLE("update_table"),
    CREATE_DATABASE("create_database"),
    DROP_DATABASE("drop_database");;

    private final String string;

    OceanType(String string) {
        this.string = string;
    }

    public static OceanType fromString(String s) {
        for(OceanType type : values()){
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
