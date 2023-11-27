/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.cleanroom;

import java.time.ZonedDateTime;
import java.util.List;

public class CleanroomAO {
    private String cleanroomId;
    private String name;
    private String orgId;
    private List<String> iam;
    private ZonedDateTime modified;
    private ZonedDateTime created;

    public String getCleanroomId() {
        return cleanroomId;
    }

    public void setCleanroomId(String cleanroomId) {
        this.cleanroomId = cleanroomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public List<String> getIam() {
        return iam;
    }

    public void setIam(List<String> iam) {
        this.iam = iam;
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
}
