/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.confirm;

import com.mytiki.account.utilities.converter.MapConvert;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@Table(name = "confirm")
public class ConfirmDO implements Serializable {
    private Long id;
    private String token;
    private ConfirmAction action;
    private Map<String, String> properties;
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

    @Column(name = "action")
    @Convert(converter = ConfirmActionConvert.class)
    public ConfirmAction getAction() {
        return action;
    }

    public void setAction(ConfirmAction action) {
        this.action = action;
    }

    @Column(name = "properties")
    @Convert(converter = MapConvert.class)
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Column(name = "created_utc")
    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }
}
