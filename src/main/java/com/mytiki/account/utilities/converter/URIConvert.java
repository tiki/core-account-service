package com.mytiki.account.utilities.converter;

import jakarta.persistence.AttributeConverter;

import java.net.URI;
import java.net.URISyntaxException;


public class URIConvert implements AttributeConverter<URI, String> {

    @Override
    public String convertToDatabaseColumn(URI attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public URI convertToEntityAttribute(String dbData) {
        if(dbData == null) return null;
        else {
            try {
                return new URI(dbData);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
