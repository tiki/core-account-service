/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class JWTConfig {
    @Bean
    public JWKSController jwks(@Autowired JWKSet jwkSet) {
        return new JWKSController(jwkSet);
    }

    @Bean
    public JWKSet jwkSet(
            @Value("${com.mytiki.l0_auth.jwt.private_key}") String pkcs8,
            @Value("${com.mytiki.l0_auth.jwt.kid}") String kid)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        ECPrivateKey privateKey = privateKey(keyFactory, pkcs8);
        ECKey.Builder keyBuilder = new ECKey.Builder(Curve.P_256, publicKey(keyFactory, privateKey));
        keyBuilder.keyUse(KeyUse.SIGNATURE);
        keyBuilder.keyID(kid);
        keyBuilder.privateKey(privateKey);
        return new JWKSet(keyBuilder.build());
    }

    @Bean
    public JWSSigner jwsSigner(
            @Autowired JWKSet jwkSet,
            @Value("${com.mytiki.l0_auth.jwt.kid}") String kid)
            throws JOSEException {
        return new ECDSASigner(jwkSet.getKeyByKeyId(kid).toECKey().toECPrivateKey(), Curve.P_256);
    }

    @Bean
    public JwtDecoder jwtDecoder(@Autowired JWKSet jwkSet) {
        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        ImmutableJWKSet<SecurityContext> immutableJWKSet = new ImmutableJWKSet<>(jwkSet);
        jwtProcessor.setJWSKeySelector(
                new JWSVerificationKeySelector<>(JWSAlgorithm.ES256, immutableJWKSet));
        return new NimbusJwtDecoder(jwtProcessor);
    }

    private ECPrivateKey privateKey(KeyFactory keyFactory, String pkcs8) throws InvalidKeySpecException {
        EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pkcs8));
        return (ECPrivateKey) keyFactory.generatePrivate(encodedKeySpec);
    }

    private ECPublicKey publicKey(KeyFactory keyFactory, ECPrivateKey privateKey) throws InvalidKeySpecException {
        ECParameterSpec keyParams = EC5Util.convertSpec(privateKey.getParams());
        ECPoint q = keyParams.getG().multiply(privateKey.getS());
        ECPoint bcW = keyParams.getCurve().decodePoint(q.getEncoded(false));
        java.security.spec.ECPoint w = new java.security.spec.ECPoint(
                bcW.getAffineXCoord().toBigInteger(),
                bcW.getAffineYCoord().toBigInteger());

        ECNamedCurveParameterSpec curveParams = ECNamedCurveTable.getParameterSpec(Curve.P_256.getStdName());
        ECNamedCurveSpec curveSpec = new ECNamedCurveSpec(curveParams.getName(), curveParams.getCurve(),
                curveParams.getG(), curveParams.getN(), curveParams.getH(), curveParams.getSeed());

        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(w, curveSpec);
        return (ECPublicKey) keyFactory.generatePublic(publicKeySpec);
    }
}
