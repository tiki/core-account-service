/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.org;

import java.time.ZonedDateTime;
import java.util.Set;

public class OrgAO {
    private String orgId;
    private String billingId;
    private Set<String> users;
    private Set<String> providers;
    private Set<String> cleanrooms;
    private ZonedDateTime modified;
    private ZonedDateTime created;

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getBillingId() {
        return billingId;
    }

    public void setBillingId(String billingId) {
        this.billingId = billingId;
    }

    public ZonedDateTime getModified() {
        return modified;
    }

    public void setModified(ZonedDateTime modified) {
        this.modified = modified;
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }

    public Set<String> getProviders() {
        return providers;
    }

    public void setProviders(Set<String> providers) {
        this.providers = providers;
    }

    public Set<String> getCleanrooms() {
        return cleanrooms;
    }

    public void setCleanrooms(Set<String> cleanrooms) {
        this.cleanrooms = cleanrooms;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }
}
