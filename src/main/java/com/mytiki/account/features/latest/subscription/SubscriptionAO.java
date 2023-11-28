/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import com.mytiki.account.features.latest.ocean.OceanAO;
import com.mytiki.account.features.latest.ocean.OceanAOReq;

import java.time.ZonedDateTime;
import java.util.List;

public class SubscriptionAO {
    private String subscriptionId;
    private String cleanroomId;
    private String query;
    private String status;
    private List<OceanAO> results;
    private ZonedDateTime created;
    private ZonedDateTime modified;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getCleanroomId() {
        return cleanroomId;
    }

    public void setCleanroomId(String cleanroomId) {
        this.cleanroomId = cleanroomId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OceanAO> getResults() {
        return results;
    }

    public void setResults(List<OceanAO> results) {
        this.results = results;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    public ZonedDateTime getModified() {
        return modified;
    }

    public void setModified(ZonedDateTime modified) {
        this.modified = modified;
    }
}
