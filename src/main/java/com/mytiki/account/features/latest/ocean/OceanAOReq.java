/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OceanAOReq {
    private String requestId;
    private String resultUri;

    @JsonCreator
    public OceanAOReq(
            @JsonProperty(required = true) String requestId,
            @JsonProperty(required = true) String resultUri) {
        this.requestId = requestId;
        this.resultUri = resultUri;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getResultUri() {
        return resultUri;
    }

    public void setResultUri(String resultUri) {
        this.resultUri = resultUri;
    }
}
