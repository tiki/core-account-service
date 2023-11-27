/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;

import java.util.List;

public class ListConvert implements AttributeConverter<List<String>, String> {
    private final ObjectMapper mapper = new ObjectMapper();
    private final TypeReference<List<String>> typeRef = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            return attribute != null ? mapper.writeValueAsString(attribute) : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            return dbData != null ? mapper.readValue(dbData, typeRef) : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
