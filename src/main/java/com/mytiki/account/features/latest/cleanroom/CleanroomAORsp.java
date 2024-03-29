/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.cleanroom;

import com.mytiki.account.features.latest.event.ao.EventAOBase;
import com.mytiki.account.features.latest.event.ao.EventAORsp;

import java.time.ZonedDateTime;
import java.util.List;

public class CleanroomAORsp extends CleanroomAO {
    private String orgId;
    private String aws;
    private ZonedDateTime modified;
    private ZonedDateTime created;
    private List<EventAORsp<? extends EventAOBase>> events;

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getAws() {
        return aws;
    }

    public void setAws(String aws) {
        this.aws = aws;
    }

    public ZonedDateTime getModified() {
        return modified;
    }

    public void setModified(ZonedDateTime modified) {
        this.modified = modified;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    public List<EventAORsp<? extends EventAOBase>> getEvents() {
        return events;
    }

    public void setEvents(List<EventAORsp<? extends EventAOBase>> events) {
        this.events = events;
    }
}
