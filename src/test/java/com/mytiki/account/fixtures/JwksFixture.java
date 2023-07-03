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
    String RS256 = """
            {
                "keys": [
                    {
                        "p": "30DlK7TX5mHgHQhDHqIT8GB3fXwzMz1kHmLldJLixg3MzRliM-BU8HAs-sZ01TkgQD8vMbv6FZzJEuO4CLoCaGlLKfRPjACvjMF2NrlvCIIFUguRgr3BvyK98mRA4g7ChA_K7_HPuL5v24bjyF_zEtUZJ3sy0rEjyRAx8hAXlE0",
                        "kty": "RSA",
                        "q": "mpS56bvvvis0zFBOUz3OHQFY_sOcsUHqMgsWYW9kxGWCZenmmGqiC7_7igrh303ev3pVXxiZuOjtMSS6ug_Nk5gfLVh7yvtcLEYqdeATBVSLh8nLLagSO7dJBcJTOXG-HATUG78KdeUXv7Mqim0NvEmD_HRVf9kJ8cZZzO8ysKk",
                        "d": "OXDcNUGLH6I5ljfFUCDx7LkN8GvZ4TCo75kM0pnHxbhiSTbVClK0bWf5zMox5JdZ4EuB7G9PWFt41QKNbTYrwxhLJDX9q3do6vGePWH_v3kMC77uZq7vg-r47Y35Uy2wmZQk_jT7KLRNt5fsAbgHv9splT70lMILStLCpqOYPVgfWWdBT-woG3SqiXFPaz_iJkEWoB6gKJ3hfKwYo_av07P3Bnrc_kZ9dgYSiyT8MR5hMwzS8g-96mHwNR1XW9C3fy0IiOdWqinngWBS0cQTDDbo-JQTS39RFxD_pgEIi_ejotpQEFrXlk4WHjwp8nwKgsiMKZToPP6LuFrTvXBXYQ",
                        "e": "AQAB",
                        "use": "sig",
                        "kid": "76e77cea-5ee2-478f-a16e-93eb12c1dd45",
                        "qi": "OJFOfEV5TrcMxlbOD1aH2IXPCiDCaTnt6zdGgVv1Sr1HsZDlQof9U1G5H4oBFpvtFgOMe4mVDpqZatJAzSLcPTRHiCIhCz_pOPqFYJiJ7gzx2ctgCwCtGuIWtbcrgGjATXJ24kjDmvo9gdJx-SbLg269wWQKo3Uh79xj5Az23uQ",
                        "dp": "uaRc7FsUrJ32ni2gonhj3B5bPh1o9dK2zg2uf6EksUwIYQQahMil2MlunZkozaUTDFl-BP0ql44oJWz2O0txdSEZP2nIO8LWN1Un15mampiDlBXKic0Ars9U45o52cAsP2Rie-O3twekPAeOobAnkCFjKVFokYp7F1ZAMejvsoE",
                        "alg": "RS256",
                        "dq": "Iepdu_2jBTtfkzBPbw4RaeXAy-zJNU77_kzWdTxGhJys9oVSNcC3mxJdMxVeJ2tjYumJT5sLJznbyLuBSI9tEGQA-yb9yjRKLeCbMk-efL3m-zz4GiVVEssM93mCXwkop-cbTpckyWchRcsem06AA_6xObOgirNo7iYRz9fvbDk",
                        "n": "hs69goOhErRoVEeAY4FmBWZADpxmGQzhoXcryh6V_hzqAPIA8hc4ATa7OVLjitoUJY_56Y0X-EB55PNTUgkVrJhraPfUq3oox6cfpVrU-PUh2BzEyL3uqb-Pk1_adfm-7Emn_v3I61dEuiZxyFqpLKT9bX3I0_AfXEnwhRhBgCkMf-JXquV5S7KXR8Dy90yi4rErGeqSgZRtcL1DF63f7lMxmumP5jEc_mu4nZfvNXm8M5Ku-x-cDDSr5B9zqBrEDkqSwl197FQygn80rnkRevJBoaF665rDJRKN6Nn32ZMhdKGQ4ZEpyApfzCHQpdcBcBp-vXXzA7DSO35Hr4_W1Q"
                    }
                ]
            }""";

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
