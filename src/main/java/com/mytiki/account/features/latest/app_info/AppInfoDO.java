/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.app_info;

import com.mytiki.account.features.latest.org_info.OrgInfoDO;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_info")
public class AppInfoDO implements Serializable {
    private Long id;
    private UUID appId;
    private String name;
    private OrgInfoDO org;
    private String jwksEndpoint;
    private Boolean verifySub;
    private ZonedDateTime created;
    private ZonedDateTime modified;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_info_id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    @Column(name = "app_id")
    public UUID getAppId() {
        return appId;
    }

    public void setAppId(UUID appId) {
        this.appId = appId;
    }

    @Column(name = "app_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "org_info_id")
    public OrgInfoDO getOrg() {
        return org;
    }

    public void setOrg(OrgInfoDO org) {
        this.org = org;
    }

    @Column(name = "jwks_endpoint")
    public String getJwksEndpoint() {
        return jwksEndpoint;
    }

    public void setJwksEndpoint(String jwksEndpoint) {
        this.jwksEndpoint = jwksEndpoint;
    }

    @Column(name = "jwks_verify_sub")
    public Boolean getVerifySub() {
        return verifySub;
    }

    public void setVerifySub(Boolean verifySub) {
        this.verifySub = verifySub;
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