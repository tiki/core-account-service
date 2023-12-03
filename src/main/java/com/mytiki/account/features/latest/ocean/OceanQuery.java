/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

public class OceanQuery {

    public static String count(String query) {
        return "SELECT COUNT(*) as \"total\" FROM (" +
                query +
                ");";
    }

    public static String sample(String query) {
        return "SELECT * FROM (" +
                query +
                ") LIMIT 10;";
    }

    public static String ctas(String query, String bucket, String cleanroomId, String table) {
        return "CREATE TABLE cr_" + cleanroomId.replace("-", "_") + "." + table +
                " WITH (" +
                "table_type = 'ICEBERG'," +
                "is_external = false," +
                "format = 'PARQUET'," +
                "location = 's3://" + bucket + "/cleanroom/" + cleanroomId + "/') " +
                "AS (" + query + ")";
    }

    public static String database(String cleanroomId) {
        return "CREATE DATABASE cr_" + cleanroomId.replace("-", "_");
    }
}
