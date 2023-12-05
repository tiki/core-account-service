/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.readme;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.api_key.ApiKeyDO;
import com.mytiki.account.features.latest.api_key.ApiKeyService;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import com.mytiki.account.utilities.facade.StripeF;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.stripe.exception.StripeException;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@XRayEnabled
public class ReadmeService {
    protected static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final JWSSigner signer;
    private final ObjectMapper mapper;
    private final SecretKeySpec secretKeySpec;
    private final ApiKeyService apiKeyService;
    private final StripeF stripe;

    public ReadmeService(String secret, ApiKeyService apiKeyService, StripeF stripe) {
        try {
            this.mapper = new ObjectMapper();
            signer = new MACSigner(secret);
            secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            this.apiKeyService = apiKeyService;
            this.stripe = stripe;
        } catch (KeyLengthException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public ReadmeAORsp personalize(ReadmeAOReq req, String signature) {
        if(!verify(req, signature))
            throw new ErrorBuilder(HttpStatus.UNAUTHORIZED)
                    .message("Verification failed")
                    .help("Check signature")
                    .exception();
        List<ApiKeyDO> keys = apiKeyService.getByEmail(req.getEmail());
        if(keys.isEmpty()) throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                .message("User does not have an API Key")
                .exception();

        ReadmeAORsp rsp = new ReadmeAORsp();
        ProfileDO profile = keys.get(0).getProfile();
        rsp.setEmail(profile.getEmail());
        rsp.setName(profile.getEmail());
        rsp.setVersion(1);
        try {
            rsp.setBilling(stripe.portal(profile.getOrg().getBillingId()));
        }catch (StripeException | NullPointerException e){
            logger.warn("Failed to get billing portal. Skipping", e);
        }

        String providerId = profile.getOrg().getProviders() != null && !profile.getOrg().getProviders().isEmpty() ?
                profile.getOrg().getProviders().get(0).getProviderId().toString() : null;
        ReadmeAORspParams params = new ReadmeAORspParams();
        params.setOrgId(profile.getOrg().getOrgId().toString());
        params.setProviderId(providerId);
        params.setUserId(profile.getUserId().toString());
        rsp.setParameters(params);

        rsp.setKeys(keys.stream().map((key) -> {
            ReadmeAORspKey rspKey = new ReadmeAORspKey();
            rspKey.setApiKey(key.getToken());
            rspKey.setKey(key.getToken());
            rspKey.setName(key.getLabel());
            return rspKey;
        }).collect(Collectors.toList()));

        return rsp;
    }

    public String authorize(ProfileDO profile) {
        try {
            ReadmeAORsp payload = new ReadmeAORsp();
            payload.setName(profile.getEmail());
            payload.setEmail(profile.getEmail());
            payload.setVersion(1);
            JWSObject jws = new JWSObject(
                    new JWSHeader
                            .Builder(JWSAlgorithm.HS256)
                            .type(JOSEObjectType.JWT)
                            .build(),
                    new Payload(mapper.writeValueAsString(payload)));
            jws.sign(signer);
            return jws.serialize();
        } catch (JsonProcessingException | JOSEException e){
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .cause(e)
                    .exception();
        }
    }


    private boolean verify(ReadmeAOReq req, String signature) {
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
