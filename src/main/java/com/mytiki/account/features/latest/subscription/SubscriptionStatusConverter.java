/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import jakarta.persistence.AttributeConverter;

public class SubscriptionStatusConverter implements AttributeConverter<SubscriptionStatus, String> {
    @Override
    public String convertToDatabaseColumn(SubscriptionStatus type) {
        return type != null ? type.toString() : null;
    }

    @Override
    public SubscriptionStatus convertToEntityAttribute(String dbData) {
        return dbData != null ? SubscriptionStatus.fromString(dbData) : null;
    }
}
