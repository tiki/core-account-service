/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.readme;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadmeAORspParams {
    @JsonProperty("user-id")
    private String userId;
    @JsonProperty("org-id")
    private String orgId;
    @JsonProperty("provider-id")
    private String providerId;
    @JsonProperty("cleanroom-id")
    private String cleanroomId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getCleanroomId() {
        return cleanroomId;
    }

    public void setCleanroomId(String cleanroomId) {
        this.cleanroomId = cleanroomId;
    }
}
