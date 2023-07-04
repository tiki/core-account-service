/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.facade;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.signers.RSADigestSigner;

import java.io.IOException;

public class RsaF {
    public static RSAPublicKey decodePublicKey(byte[] publicKey) throws IOException {
        try (ASN1InputStream inputStream = new ASN1InputStream(publicKey)) {
            ASN1Sequence topLevelSeq = ASN1Sequence.getInstance(inputStream.readObject());
            ASN1Sequence algorithmSeq = ASN1Sequence.getInstance(topLevelSeq.getObjectAt(0));
            ASN1BitString publicKeyBitString = ASN1BitString.getInstance(topLevelSeq.getObjectAt(1));
            ASN1Sequence publicKeySeq = ASN1Sequence.getInstance(
                    ASN1Primitive.fromByteArray(publicKeyBitString.getBytes()));
            ASN1Integer modulus = ASN1Integer.getInstance(publicKeySeq.getObjectAt(0));
            ASN1Integer exponent = ASN1Integer.getInstance(publicKeySeq.getObjectAt(1));
            return new RSAPublicKey(modulus.getValue(), exponent.getValue());
        }
    }

    public static RSAPrivateKey decodePrivateKey(byte[] privateKey) throws IOException {
        try (ASN1InputStream inputStream = new ASN1InputStream(privateKey)) {
            ASN1Sequence topLevelSeq = ASN1Sequence.getInstance(inputStream.readObject());
            ASN1OctetString privateKeyOctet = ASN1OctetString.getInstance(topLevelSeq.getObjectAt(2));
            ASN1Sequence publicKeySeq = (ASN1Sequence) ASN1Sequence.fromByteArray(privateKeyOctet.getOctets());
            ASN1Integer modulus = (ASN1Integer) publicKeySeq.getObjectAt(1);
            ASN1Integer publicExponent = (ASN1Integer) publicKeySeq.getObjectAt(2);
            ASN1Integer privateExponent = (ASN1Integer) publicKeySeq.getObjectAt(3);
            ASN1Integer prime1 = (ASN1Integer) publicKeySeq.getObjectAt(4);
            ASN1Integer prime2 = (ASN1Integer) publicKeySeq.getObjectAt(5);
            ASN1Integer exponent1 = (ASN1Integer) publicKeySeq.getObjectAt(6);
            ASN1Integer exponent2 = (ASN1Integer) publicKeySeq.getObjectAt(7);
            ASN1Integer coefficient = (ASN1Integer) publicKeySeq.getObjectAt(8);
            return new RSAPrivateKey(
                    modulus.getValue(), publicExponent.getValue(), privateExponent.getValue(),
                    prime1.getValue(), prime2.getValue(), exponent1.getValue(),
                    exponent2.getValue(), coefficient.getValue());
        }
    }

    public static boolean verify(RSAPublicKey publicKey, byte[] message, byte[] signature){
        RSADigestSigner signer = new RSADigestSigner(new SHA256Digest());
        RSAKeyParameters keyParameters =
                new RSAKeyParameters(false, publicKey.getModulus(), publicKey.getPublicExponent());
        signer.init(false, keyParameters);
        signer.update(message, 0, message.length);
        return signer.verifySignature(signature);
    }

    public static RSAPrivateKey generate() throws JOSEException {
        RSAKey keypair = new RSAKeyGenerator(RSAKeyGenerator.MIN_KEY_SIZE_BITS).generate();
        return new RSAPrivateKey(
                keypair.getModulus().decodeToBigInteger(),
                keypair.getPublicExponent().decodeToBigInteger(),
                keypair.getPrivateExponent().decodeToBigInteger(),
                keypair.getFirstPrimeFactor().decodeToBigInteger(),
                keypair.getSecondPrimeFactor().decodeToBigInteger(),
                keypair.getFirstFactorCRTExponent().decodeToBigInteger(),
                keypair.getSecondFactorCRTExponent().decodeToBigInteger(),
                keypair.getFirstCRTCoefficient().decodeToBigInteger());
    }

    public static RSAPublicKey toPublic(RSAPrivateKey privateKey){
        return new RSAPublicKey(privateKey.getModulus(), privateKey.getPublicExponent());
    }
}
