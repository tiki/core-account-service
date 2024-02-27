/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.event.type;

import jakarta.persistence.AttributeConverter;

public class EventTypeConverter implements AttributeConverter<EventType, String> {
    @Override
    public String convertToDatabaseColumn(EventType type) {
        return type != null ? type.toString() : null;
    }

    @Override
    public EventType convertToEntityAttribute(String dbData) {
        return dbData != null ? EventType.fromString(dbData) : null;
    }
}
