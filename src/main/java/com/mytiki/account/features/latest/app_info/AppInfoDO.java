/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.app_info;

import com.mytiki.account.features.latest.jwks.JwksDO;
import com.mytiki.account.features.latest.org_info.OrgInfoDO;
import com.mytiki.account.utilities.converter.RsaPrivateConvert;
import jakarta.persistence.*;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;

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
    private JwksDO jwks;
    private RSAPrivateKey signKey;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jwks_id")
    public JwksDO getJwks() {
        return jwks;
    }

    public void setJwks(JwksDO jwks) {
        this.jwks = jwks;
    }

    @Column(name = "sign_key")
    @Convert(converter = RsaPrivateConvert.class)
    public RSAPrivateKey getSignKey() {
        return signKey;
    }

    public void setSignKey(RSAPrivateKey signKey) {
        this.signKey = signKey;
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
