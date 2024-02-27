/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.event.ao;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.ZonedDateTime;

public class EventAORsp<T extends EventAOBase>{
    private String requestId;
    private String status;
    private String type;
    private ZonedDateTime created;
    private ZonedDateTime modified;

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type",
            visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = EventAOCrCreateRsp.class, name = "cr_create"),
            @JsonSubTypes.Type(value = EventAOSubEstimateRsp.class, name = "sub_estimate"),
            @JsonSubTypes.Type(value = EventAOSubPurchaseRsp.class, name = "sub_purchase")
    })
    private T result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    public ZonedDateTime getModified() {
        return modified;
    }

    public void setModified(ZonedDateTime modified) {
        this.modified = modified;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
