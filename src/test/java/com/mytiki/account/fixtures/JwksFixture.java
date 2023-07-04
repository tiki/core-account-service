package com.mytiki.account.fixtures;

import com.mytiki.account.utilities.Constants;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;

import java.sql.Date;
import java.text.ParseException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public interface JwksFixture {
    String endpoint = "http://localhost:8080/.well-known/jwks.json";

    String ES256 = """
            {
                "keys": [
                    {
                        "kty": "EC",
                        "d": "RNA2EmYNa8lRkWovqQ65wyrHE-3vmgGZiX8D7LPBfmg",
                        "use": "sig",
                        "crv": "P-256",
                        "kid": "6373263a-8761-4e07-bed0-ffa0d7783741",
                        "x": "XN9VmEwJnQKDjnnBP8E6nBoMP-rPIJ28A_YAC-ZK33M",
                        "y": "v59-3V86_XTFfWBjEgHcKDC7vFBpJXUPOn6GkUiq4Tg",
                        "alg": "ES256"
                    }
                ]
            }""";

    static String jwt(String jwks, String sub) throws ParseException, JOSEException {
        JWK jwk = JWKSet.parse(jwks).getKeys().get(0);
        JWSAlgorithm alg = JWSAlgorithm.parse(jwk.getAlgorithm().toString());
        JWSSigner signer = new DefaultJWSSignerFactory().createJWSSigner(jwk);

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        JWSObject jws = new JWSObject(
                new JWSHeader
                        .Builder(alg)
                        .type(JOSEObjectType.JWT)
                        .build(),
                new Payload(
                        new JWTClaimsSet.Builder()
                                .issuer(Constants.MODULE_DOT_PATH)
                                .issueTime(java.sql.Date.from(now.toInstant()))
                                .expirationTime(Date.from(now.plusSeconds(60).toInstant()))
                                .subject(sub)
                                .build()
                                .toJSONObject()
                ));
        jws.sign(signer);
        return jws.serialize();
    }
}
