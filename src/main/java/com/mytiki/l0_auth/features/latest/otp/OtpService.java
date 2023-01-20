/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.otp;

import com.mytiki.l0_auth.features.latest.refresh.RefreshService;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoAO;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoService;
import com.mytiki.l0_auth.utilities.Constants;
import com.mytiki.l0_auth.utilities.JWSBuilder;
import com.mytiki.l0_auth.utilities.Mustache;
import com.mytiki.l0_auth.utilities.Sendgrid;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

public class OtpService {
    private static final Long CODE_EXPIRY_DURATION_MINUTES = 30L;
    private final OtpRepository repository;
    private final Mustache templates;
    private final Sendgrid sendgrid;
    private final JWSSigner signer;
    private final RefreshService refreshService;
    private final UserInfoService userInfoService;

    public OtpService(
            OtpRepository repository,
            Mustache templates,
            Sendgrid sendgrid,
            JWSSigner signer,
            RefreshService refreshService,
            UserInfoService userInfoService) {
        this.repository = repository;
        this.templates = templates;
        this.sendgrid = sendgrid;
        this.signer = signer;
        this.refreshService = refreshService;
        this.userInfoService = userInfoService;
    }

    public OtpAOStartRsp start(OtpAOStartReq req) {
        String deviceId = randomB64(32);
        String code = randomAlphanumeric(6);
        req.setEmail(req.getEmail().toLowerCase());
        if (sendEmail(req.getEmail(), code)) {
            OtpDO otpDO = new OtpDO();
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            ZonedDateTime expires = now.plusMinutes(CODE_EXPIRY_DURATION_MINUTES);
            otpDO.setOtpHashed(hashedOtp(deviceId, code));
            otpDO.setIssued(now);
            otpDO.setExpires(expires);
            if(req.isNotAnonymous()) otpDO.setEmail(req.getEmail());
            repository.save(otpDO);
            OtpAOStartRsp rsp = new OtpAOStartRsp();
            rsp.setDeviceId(deviceId);
            rsp.setExpires(expires);
            return rsp;
        } else {
            throw new ApiExceptionBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("One-time Password (OTP) failed")
                    .detail("Issue with sending email")
                    .build();
        }
    }

    public OAuth2AccessTokenResponse authorize(String deviceId, String code, List<String> audience) {
        String hashedOtp = hashedOtp(deviceId, code);
        Optional<OtpDO> found = repository.findByOtpHashed(hashedOtp);
        if (found.isEmpty())
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.ACCESS_DENIED,
                    "deviceId (username) and/or code (password) are invalid",
                    null
            ));
        repository.delete(found.get());
        if (ZonedDateTime.now(ZoneOffset.UTC).isAfter(found.get().getExpires()))
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.ACCESS_DENIED,
                    "Expired code. Re-start OTP process",
                    null
            ));
        try {
            String subject = null;
            if(found.get().getEmail() != null) {
                UserInfoAO userInfo = userInfoService.createIfNotExists(found.get().getEmail());
                subject = userInfo.getSub();
            }

            if(audience != null && audience.contains("storage.l0.mytiki.com") && subject == null)
                throw new OAuth2AuthorizationException(new OAuth2Error(
                        OAuth2ErrorCodes.ACCESS_DENIED),
                        "storage.l0.mytiki.com does not support anonymous subjects");

            JWSObject token = new JWSBuilder()
                    .expIn(Constants.TOKEN_EXPIRY_DURATION_SECONDS)
                    .sub(subject)
                    .aud(audience)
                    .build(signer);

            return OAuth2AccessTokenResponse
                    .withToken(token.serialize())
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .expiresIn(Constants.TOKEN_EXPIRY_DURATION_SECONDS)
                    //.scopes()
                    .refreshToken(refreshService.issue(subject, audience))
                    .build();
        } catch (JOSEException e) {
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "Issue with JWT construction",
                    null
            ), e);
        }
    }

    private boolean sendEmail(String email, String code) {
        if(!EmailValidator.getInstance().isValid(email))
            throw new ApiExceptionBuilder(HttpStatus.BAD_REQUEST)
                    .message("Invalid email")
                    .build();

        Map<String, String> input = new HashMap<>(1);
        input.put("OTP", code);

        return sendgrid.send(email,
                templates.resovle(OtpConfig.TEMPLATE_SUBJECT, null),
                templates.resovle(OtpConfig.TEMPLATE_BODY_HTML, input),
                templates.resovle(OtpConfig.TEMPLATE_BODY_TXT, input));
    }

    private String hashedOtp(String deviceId, String code) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(code.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(md.digest(deviceId.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new ApiExceptionBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("One-time Password (OTP) failed")
                    .detail("Issue with SHA256")
                    .cause(e)
                    .build();
        }
    }

    private String randomB64(int len) {
        try {
            byte[] bytes = new byte[len];
            SecureRandom.getInstanceStrong().nextBytes(bytes);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new ApiExceptionBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("One-time Password (OTP) failed")
                    .detail("Issue with SecureRandom generation")
                    .cause(e)
                    .build();
        }
    }

    private String randomAlphanumeric(int len) {
        try {
            int[] ints = SecureRandom.getInstanceStrong().ints(len, 0, 36).toArray();
            StringBuilder result = new StringBuilder();
            Arrays.stream(ints).forEach(i -> result.append(Integer.toString(i, 36)));
            return result.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new ApiExceptionBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("One-time Password (OTP) failed")
                    .detail("Issue with SecureRandom generation")
                    .cause(e)
                    .build();
        }
    }
}
