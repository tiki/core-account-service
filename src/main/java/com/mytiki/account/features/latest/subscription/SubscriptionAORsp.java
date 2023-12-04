/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import java.util.List;

public class SubscriptionAORsp extends SubscriptionAO {
    private String query;
    private List<SubscriptionAORspCount> count;
    private List<SubscriptionAORspSample> sample;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<SubscriptionAORspCount> getCount() {
        return count;
    }

    public void setCount(List<SubscriptionAORspCount> count) {
        this.count = count;
    }

    public List<SubscriptionAORspSample> getSample() {
        return sample;
    }

    public void setSample(List<SubscriptionAORspSample> sample) {
        this.sample = sample;
    }
}
