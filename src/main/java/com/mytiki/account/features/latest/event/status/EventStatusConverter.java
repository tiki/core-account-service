/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.event.status;

import jakarta.persistence.AttributeConverter;

public class EventStatusConverter implements AttributeConverter<EventStatus, String> {
    @Override
    public String convertToDatabaseColumn(EventStatus status) {
        return status != null ? status.toString() : null;
    }

    @Override
    public EventStatus convertToEntityAttribute(String dbData) {
        return dbData != null ? EventStatus.fromString(dbData) : null;
    }
}
