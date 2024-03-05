package com.mytiki.account.utilities;

import software.amazon.awssdk.services.s3.endpoints.internal.Value;

public class QuerySanitizer {

    public String sanitize(String query) {
        query = query.trim();
        if (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1);
        }

        for (int count = 0; count <= query.length(); count++){
            if ((count + 4) < query.length() && query.substring(count, count + 4).equalsIgnoreCase("from")){
                String trim = query.substring(count + 5).trim();
                int indexDot = trim.indexOf(".");
                int indexSpace = trim.indexOf(" ");
                if ((indexSpace > indexDot && indexDot != -1) || (indexDot != -1 && indexSpace == -1)){
                    query = query.substring(0, count + 5) + "tiki" + trim.substring(indexDot);
                } else  {
                    query = query.substring(0, count + 5) + "tiki." + query.substring(count + 5);
                }
            }
        }

        return query;
    }
}
