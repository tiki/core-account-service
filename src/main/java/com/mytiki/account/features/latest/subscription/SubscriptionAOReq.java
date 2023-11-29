/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SubscriptionAOReq {
    private String query;
    private String cleanroomId;

    @JsonCreator
    public SubscriptionAOReq(
            @JsonProperty(required = true) String query,
            @JsonProperty(required = true) String cleanroomId) {
        this.query = query;
        this.cleanroomId = cleanroomId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getCleanroomId() {
        return cleanroomId;
    }

    public void setCleanroomId(String cleanroomId) {
        this.cleanroomId = cleanroomId;
    }
}
