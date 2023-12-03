/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

public class SubscriptionAORsp extends SubscriptionAO {
    private String query;
    //private List<OceanAO> results;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

//    public List<OceanAO> getResults() {
//        return results;
//    }
//
//    public void setResults(List<OceanAO> results) {
//        this.results = results;
//    }
}
