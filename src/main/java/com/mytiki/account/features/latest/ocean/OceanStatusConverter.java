/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

import jakarta.persistence.AttributeConverter;

public class OceanStatusConverter implements AttributeConverter<OceanStatus, String> {
    @Override
    public String convertToDatabaseColumn(OceanStatus status) {
        return status != null ? status.toString() : null;
    }

    @Override
    public OceanStatus convertToEntityAttribute(String dbData) {
        return dbData != null ? OceanStatus.fromString(dbData) : null;
    }
}
