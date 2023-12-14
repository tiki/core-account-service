/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.cleanroom;

import java.time.ZonedDateTime;
import java.util.List;

public class CleanroomAO {
    private String cleanroomId;
    private String name;
    private String description;

    public String getCleanroomId() {
        return cleanroomId;
    }

    public void setCleanroomId(String cleanroomId) {
        this.cleanroomId = cleanroomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
