/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.user_info.UserInfoDO;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;

import java.util.HashMap;

@XRayEnabled
public class ReadmeF {
    private final JWSSigner signer;
    private final ObjectMapper mapper;

    public ReadmeF(String secret) {
        try {
            this.mapper = new ObjectMapper();
            signer = new MACSigner(secret);
        } catch (KeyLengthException e) {
            throw new RuntimeException(e);
        }
    }

    public String sign(UserInfoDO user, String apiKey) throws JOSEException, JsonProcessingException {
        String appId = user.getOrg().getApps() != null && !user.getOrg().getApps().isEmpty() ?
                user.getOrg().getApps().get(0).getAppId().toString() : null;
        String payload = mapper.writeValueAsString(new HashMap<>(){{
            put("name", user.getEmail());
            put("email", user.getEmail());
            put("version", 1);
            put("apiKey", apiKey);
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
}
