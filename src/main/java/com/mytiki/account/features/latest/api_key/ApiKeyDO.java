/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.api_key;

import com.mytiki.account.features.latest.profile.ProfileDO;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "api_key")
public class ApiKeyDO implements Serializable {
    private Long id;
    private String label;
    private String token;
    private ProfileDO profile;
    private ZonedDateTime created;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "token")
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Column(name = "label")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @ManyToOne
    @JoinColumn(name = "profile_id")
    public ProfileDO getProfile() {
        return profile;
    }

    public void setProfile(ProfileDO profile) {
        this.profile = profile;
    }

    @Column(name = "created_utc")
    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }
}
