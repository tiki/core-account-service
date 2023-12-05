/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.org;

import com.mytiki.account.features.latest.provider.ProviderDO;
import com.mytiki.account.features.latest.profile.ProfileDO;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "org")
public class OrgDO implements Serializable {
    private Long id;
    private UUID orgId;
    private String billingId;
    private List<ProfileDO> profiles;
    private List<ProviderDO> providers;
    private ZonedDateTime created;
    private ZonedDateTime modified;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
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

    @OneToMany(mappedBy = "org", fetch = FetchType.EAGER)
    public List<ProfileDO> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<ProfileDO> users) {
        this.profiles = users;
    }

    @OneToMany(mappedBy = "org", fetch = FetchType.EAGER)
    public List<ProviderDO> getProviders() {
        return providers;
    }

    public void setProviders(List<ProviderDO> apps) {
        this.providers = apps;
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
