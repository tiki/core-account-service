/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.cleanroom;

import com.mytiki.account.features.latest.ocean.OceanDO;
import com.mytiki.account.features.latest.org.OrgDO;
import com.mytiki.account.utilities.converter.ListConvert;
import jakarta.persistence.*;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cleanroom")
public class CleanroomDO implements Serializable {
    private Long id;
    private UUID cleanroomId;
    private String name;
    private String aws;
    private String description;
    private OrgDO org;
    private ZonedDateTime created;
    private ZonedDateTime modified;
    private OceanDO result;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "cleanroom_id")
    public UUID getCleanroomId() {
        return cleanroomId;
    }

    public void setCleanroomId(UUID cleanroomId) {
        this.cleanroomId = cleanroomId;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "aws_account")
    public String getAws() {
        return aws;
    }

    public void setAws(String aws) {
        this.aws = aws;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "org_id")
    public OrgDO getOrg() {
        return org;
    }

    public void setOrg(OrgDO org) {
        this.org = org;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "cleanroom_ocean",
            joinColumns = @JoinColumn(name = "cleanroom_id"),
            inverseJoinColumns = @JoinColumn(name = "ocean_id")
    )
    public OceanDO getResult() {
        return result;
    }

    public void setResult(OceanDO result) {
        this.result = result;
    }
}
