/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.cleanroom;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

public class CleanroomAOReq {
    @Parameter(description = "A user friendly name describing your cleanroom")
    private String name;
    @Parameter(description = "A list of ARNs for IAM accounts to access the cleanroom")
    private List<String> iam;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIam() {
        return iam;
    }

    public void setIam(List<String> iam) {
        this.iam = iam;
    }
}
