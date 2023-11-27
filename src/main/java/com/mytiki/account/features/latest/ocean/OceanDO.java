/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

import com.mytiki.account.features.latest.subscription.SubscriptionDO;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "ocean")
public class OceanDO implements Serializable {
    private Long id;
    private UUID requestId;
    private SubscriptionDO subscription;
    private OceanStatus status;
    private OceanType type;
    private String executionArn;
    private String resultUri;
    private String result;
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

    @Column(name = "request_id")
    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    public SubscriptionDO getSubscription() {
        return subscription;
    }

    public void setSubscription(SubscriptionDO subscription) {
        this.subscription = subscription;
    }

    @Column(name = "status")
    @Convert(converter = OceanStatusConverter.class)
    public OceanStatus getStatus() {
        return status;
    }

    public void setStatus(OceanStatus status) {
        this.status = status;
    }

    @Column(name = "type")
    @Convert(converter = OceanTypeConverter.class)
    public OceanType getType() {
        return type;
    }

    public void setType(OceanType type) {
        this.type = type;
    }

    @Column(name = "execution_arn")
    public String getExecutionArn() {
        return executionArn;
    }

    public void setExecutionArn(String executionArn) {
        this.executionArn = executionArn;
    }

    @Column(name = "result_uri")
    public String getResultUri() {
        return resultUri;
    }

    public void setResultUri(String resultUri) {
        this.resultUri = resultUri;
    }

    @Column(name = "result")
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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
