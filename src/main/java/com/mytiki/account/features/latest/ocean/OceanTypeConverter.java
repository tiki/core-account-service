/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

import jakarta.persistence.AttributeConverter;

public class OceanTypeConverter implements AttributeConverter<OceanType, String> {
    @Override
    public String convertToDatabaseColumn(OceanType type) {
        return type.toString();
    }

    @Override
    public OceanType convertToEntityAttribute(String dbData) {
        return dbData != null ? OceanType.fromString(dbData) : null;
    }
}
