/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.org_info;

import com.mytiki.l0_auth.features.latest.app_info.AppInfoDO;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoDO;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "org_info")
public class OrgInfoDO implements Serializable {
    private Long id;
    private UUID orgId;
    private String billingId;
    private List<UserInfoDO> users;
    private List<AppInfoDO> apps;
    private ZonedDateTime created;
    private ZonedDateTime modified;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "org_info_id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "org_id")
    public UUID getOrgId() {
        return orgId;
    }

    public void setOrgId(UUID orgId) {
        this.orgId = orgId;
    }

    @Column(name = "billing_id")
    public String getBillingId() {
        return billingId;
    }

    public void setBillingId(String billingId) {
        this.billingId = billingId;
    }

    @OneToMany(mappedBy = "org")
    public List<UserInfoDO> getUsers() {
        return users;
    }

    public void setUsers(List<UserInfoDO> users) {
        this.users = users;
    }

    @OneToMany(mappedBy = "org")
    public List<AppInfoDO> getApps() {
        return apps;
    }

    public void setApps(List<AppInfoDO> apps) {
        this.apps = apps;
    }

    @Column(name = "created_utc")
    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    @Column(name = "modified_utc")
    public ZonedDateTime getModified() {
        return modified;
    }

    public void setModified(ZonedDateTime modified) {
        this.modified = modified;
    }
}
