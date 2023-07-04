package com.mytiki.account.utilities.converter;

import jakarta.persistence.AttributeConverter;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;

import java.io.IOException;


public class RsaPrivateConvert implements AttributeConverter<RSAPrivateKey, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(RSAPrivateKey attribute) {
        if(attribute == null) return null;
        else {
            try{
                return attribute.getEncoded();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public RSAPrivateKey convertToEntityAttribute(byte[] dbData) {
        if(dbData == null) return null;
        else {
            try {
                return RSAPrivateKey.getInstance(ASN1Primitive.fromByteArray(dbData));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
