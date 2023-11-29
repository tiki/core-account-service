/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.cleanroom;

import java.util.List;

public class CleanroomAOReq {
    private String name;
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
