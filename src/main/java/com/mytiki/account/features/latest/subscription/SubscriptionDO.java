/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import com.mytiki.account.features.latest.ocean.OceanDO;
import com.mytiki.account.features.latest.ocean.OceanStatusConverter;
import com.mytiki.account.features.latest.profile.ProfileDO;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "subscription")
public class SubscriptionDO implements Serializable {
    private Long id;
    private ProfileDO profile;
    private String query;
    private SubscriptionStatus status;
    private List<OceanDO> results;
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_id")
    public ProfileDO getProfile() {
        return profile;
    }

    public void setProfile(ProfileDO profile) {
        this.profile = profile;
    }

    @Column(name = "query")
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Column(name = "status")
    @Convert(converter = SubscriptionStatusConverter.class)
    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    @OneToMany(mappedBy = "subscription")
    public List<OceanDO> getResults() {
        return results;
    }

    public void setResults(List<OceanDO> results) {
        this.results = results;
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
