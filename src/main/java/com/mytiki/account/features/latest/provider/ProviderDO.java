/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.provider;

import com.mytiki.account.features.latest.org.OrgDO;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "provider")
public class ProviderDO implements Serializable {
    private Long id;
    private UUID providerId;
    private String name;
    private OrgDO org;
    private String pubKey;
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

    @Column(name = "provider_id")
    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID appId) {
        this.providerId = appId;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "org_id")
    public OrgDO getOrg() {
        return org;
    }

    public void setOrg(OrgDO org) {
        this.org = org;
    }

    @Column(name = "pub_key")
    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubId) {
        this.pubKey = pubId;
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
