/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.api_key;

import com.mytiki.l0_auth.features.latest.app_info.AppInfoDO;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_key")
public class ApiKeyDO implements Serializable {
    private UUID id;
    private String hashedSecret;
    private AppInfoDO app;
    private ZonedDateTime created;

    @Id
    @Column(name = "api_key_id")
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Column(name = "secret_hash")
    public String getHashedSecret() {
        return hashedSecret;
    }

    public void setHashedSecret(String hashedSecret) {
        this.hashedSecret = hashedSecret;
    }

    @ManyToOne
    @JoinColumn(name = "app_info_id")
    public AppInfoDO getApp() {
        return app;
    }

    public void setApp(AppInfoDO app) {
        this.app = app;
    }

    @Column(name = "created_utc")
    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }
}
