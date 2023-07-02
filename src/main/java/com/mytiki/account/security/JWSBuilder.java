/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.security;

import com.mytiki.account.utilities.Constants;
import com.nimbusds.jose.*;
import com.nimbusds.jwt.JWTClaimsSet;

import java.sql.Date;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

public class JWSBuilder {
    private String sub;
    private List<String> aud;
    private String jti;
    private ZonedDateTime exp;
    private Long expIn;
    private ZonedDateTime iat;
    private List<String> scp;

    public JWSBuilder iat(ZonedDateTime instant){
        this.iat = instant;
        return this;
    }

    public JWSBuilder expIn(Long seconds){
        this.expIn = seconds;
        return this;
    }

    public JWSBuilder exp(ZonedDateTime instant){
        this.exp = instant;
        return this;
    }

    public JWSBuilder jti(String id){
        this.jti = id;
        return this;
    }

    public JWSBuilder sub(String subject){
        this.sub = subject;
        return this;
    }

    public JWSBuilder scp(List<String> scp){
        if(scp != null && !scp.isEmpty())
            this.scp = scp;
        return this;
    }

    public JWSBuilder aud(List<String> audiences){
        if(audiences != null && !audiences.isEmpty())
            this.aud = audiences;
        return this;
    }

    public JWSObject build(JWSSigner signer) throws JOSEException {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        if(iat == null) iat = now;
        if(exp == null && expIn != null) exp = now.plusSeconds(expIn);
        JWSObject jws = new JWSObject(
                new JWSHeader
                        .Builder(JWSAlgorithm.ES256)
                        .type(JOSEObjectType.JWT)
                        .keyID(JWTConfig.KID)
                        .build(),
                new Payload(
                        new JWTClaimsSet.Builder()
                                .issuer(Constants.MODULE_DOT_PATH)
                                .issueTime(Date.from(iat.toInstant()))
                                .expirationTime(exp == null ? null : Date.from(exp.toInstant()))
                                .subject(sub)
                                .audience(aud)
                                .jwtID(jti)
                                .claim("scp", scp)
                                .build()
                                .toJSONObject()
                ));
        jws.sign(signer);
        return jws;
    }
}
