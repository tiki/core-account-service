/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import com.mytiki.account.features.latest.event.ao.EventAOBase;
import com.mytiki.account.features.latest.event.ao.EventAORsp;

import java.util.List;

public class SubscriptionAORsp extends SubscriptionAO {
    private String query;
    private List<EventAORsp<? extends EventAOBase>> events;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<EventAORsp<? extends EventAOBase>> getEvents() {
        return events;
    }

    public void setEvents(List<EventAORsp<? extends EventAOBase>> events) {
        this.events = events;
    }
}
