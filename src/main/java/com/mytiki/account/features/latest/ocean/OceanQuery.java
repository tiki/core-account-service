/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

public class OceanQuery {

    public static String wrapCount(String query) {
        return "SELECT COUNT(*) as \"total\" FROM (" +
                query +
                ");";
    }

    public static String wrapSample(String query) {
        return "SELECT * FROM (" +
                query +
                ") LIMIT 3;";
    }

    public static String wrapCreate(String query, String bucket, String cleanroomId, String table) {
        return "CREATE TABLE " + table(cleanroomId, table) +
                " WITH (" +
                "table_type = 'ICEBERG'," +
                "is_external = false," +
                "format = 'PARQUET'," +
                "location = 's3://" + bucket + "/cleanroom/" + cleanroomId + "/') " +
                "AS (" + query + ")";
    }

    public static String createDatabase(String cleanroomId) {
        return "CREATE DATABASE cr_" + cleanroomId.replace("-", "_");
    }

    public static String dropDatabase(String cleanroomId) {
        return "DROP DATABASE cr_" + cleanroomId.replace("-", "_");
    }

    public static String table(String cleanroomId, String table) {
        return "cr_" + cleanroomId.replace("-", "_")
                + "." + table.replace("-", "_");
    }

    public static String sample(String table){
        return "SELECT COUNT(*) FROM " + table;
    }

    public static String count(String table){
        return "SELECT * FROM " + table + " LIMIT 10";
    }
}
