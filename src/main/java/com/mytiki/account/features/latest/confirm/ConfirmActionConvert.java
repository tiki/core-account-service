/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.confirm;

import jakarta.persistence.AttributeConverter;

public class ConfirmActionConvert implements AttributeConverter<ConfirmAction, String> {

    @Override
    public String convertToDatabaseColumn(ConfirmAction attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public ConfirmAction convertToEntityAttribute(String dbData) {
        return dbData != null ? ConfirmAction.find(dbData) : null;
    }
}
