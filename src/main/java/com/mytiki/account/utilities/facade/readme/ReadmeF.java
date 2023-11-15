/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.facade.readme;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.user_info.UserInfoDO;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.http.HttpStatus;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;

@XRayEnabled
public class ReadmeF {
    private final JWSSigner signer;
    private final ObjectMapper mapper;
    private final SecretKeySpec secretKeySpec;

    public ReadmeF(String secret) {
        try {
            this.mapper = new ObjectMapper();
            signer = new MACSigner(secret);
            secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        } catch (KeyLengthException e) {
            throw new RuntimeException(e);
        }
    }

    public String sign(UserInfoDO user) throws JOSEException, JsonProcessingException {
        String appId = user.getOrg().getApps() != null && !user.getOrg().getApps().isEmpty() ?
                user.getOrg().getApps().get(0).getAppId().toString() : null;
        String payload = mapper.writeValueAsString(new HashMap<>(){{
            put("name", user.getEmail());
            put("email", user.getEmail());
            put("version", 1);
            put("parameters", new HashMap<>(){{
                put("app-id", appId);
                put("user-id", user.getUserId().toString());
                put("org-id", user.getOrg().getOrgId().toString());
            }});
        }});
        JWSObject jws = new JWSObject(
                new JWSHeader
                        .Builder(JWSAlgorithm.HS256)
                        .type(JOSEObjectType.JWT)
                        .build(),
                new Payload(payload));
        jws.sign(signer);
        return jws.serialize();
    }

    public boolean verify(ReadmeReq req, String signature) {
        if (signature == null)
            throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                    .message("Missing Signature")
                    .exception();
        long timestamp = 0L;
        String readmeSignature = null;
        String expectedScheme = "v0";
        String[] split = signature.split(",");
        for (String s : split) {
            String[] kv = s.split("=");
            if (kv[0].equals("t")) timestamp = Long.parseLong(kv[1]);
            if (kv[0].equals(expectedScheme)) readmeSignature = kv[1];
        }
        if (Instant.ofEpochMilli(timestamp)
                .plusSeconds(60 * 30)
                .isBefore(ZonedDateTime.now().toInstant()))
            throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                    .message("Expired Signature")
                    .exception();
        try {
            String unsigned = timestamp + "." + mapper.writeValueAsString(req);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            String verifySignature = Hex.toHexString(mac.doFinal(unsigned.getBytes(Charset.defaultCharset())));
            return verifySignature.equals(readmeSignature);
        }catch (JsonProcessingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message(e.getMessage())
                    .exception();
        }
    }
}
