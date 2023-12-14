/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.cleanroom;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CleanroomAOReq {
    private String description;
    private String aws;

    @JsonCreator
    public CleanroomAOReq(
            @JsonProperty String description,
            @JsonProperty(required = true) String aws) {
        this.description = description;
        this.aws = aws;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAws() {
        return aws;
    }

    public void setAws(String aws) {
        this.aws = aws;
    }
}
