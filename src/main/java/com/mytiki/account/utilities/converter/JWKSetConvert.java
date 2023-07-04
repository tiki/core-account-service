package com.mytiki.account.utilities.converter;

import com.nimbusds.jose.jwk.JWKSet;
import jakarta.persistence.AttributeConverter;

import java.text.ParseException;


public class JWKSetConvert implements AttributeConverter<JWKSet, String> {

    @Override
    public String convertToDatabaseColumn(JWKSet attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public JWKSet convertToEntityAttribute(String dbData) {
        if(dbData == null) return null;
        else {
            try {
                return JWKSet.parse(dbData);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
