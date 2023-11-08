/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.otp;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.features.latest.user_info.UserInfoAO;
import com.mytiki.account.features.latest.user_info.UserInfoDO;
import com.mytiki.account.features.latest.user_info.UserInfoService;
import com.mytiki.account.security.oauth.OauthScopes;
import com.mytiki.account.security.oauth.OauthSub;
import com.mytiki.account.security.oauth.OauthSubNamespace;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import com.mytiki.account.utilities.builder.JwtBuilder;
import com.mytiki.account.utilities.facade.B64F;
import com.mytiki.account.utilities.facade.MustacheF;
import com.mytiki.account.utilities.facade.ReadmeF;
import com.mytiki.account.utilities.facade.SendgridF;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import jakarta.transaction.Transactional;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.http.HttpStatus;
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

@XRayEnabled
public class OtpService {
    private static final Long CODE_EXPIRY_DURATION_MINUTES = 30L;
    private final OtpRepository repository;
    private final MustacheF templates;
    private final SendgridF sendgrid;
    private final JWSSigner signer;
    private final RefreshService refreshService;
    private final UserInfoService userInfoService;
    private final OauthScopes allowedScopes;
    private final ReadmeF readme;

    public OtpService(
            OtpRepository repository,
            MustacheF templates,
            SendgridF sendgrid,
            JWSSigner signer,
            RefreshService refreshService,
            UserInfoService userInfoService,
            OauthScopes allowedScopes,
            ReadmeF readme) {
        this.repository = repository;
        this.templates = templates;
        this.sendgrid = sendgrid;
        this.signer = signer;
        this.refreshService = refreshService;
        this.userInfoService = userInfoService;
        this.allowedScopes = allowedScopes;
        this.readme = readme;
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
            otpDO.setEmail(req.getEmail());
            repository.save(otpDO);
            OtpAOStartRsp rsp = new OtpAOStartRsp();
            rsp.setDeviceId(deviceId);
            rsp.setExpires(expires);
            return rsp;
        } else {
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("One-time Password (OTP) failed")
                    .detail("Issue with sending email")
                    .exception();
        }
    }

    @Transactional
    public OAuth2AccessTokenResponse authorize(String deviceId, String code, String requestedScope) {
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
            OauthSub subject = new OauthSub();
            OauthScopes scopes = allowedScopes.filter(requestedScope);
            UserInfoDO userInfo = userInfoService.createIfNotExists(found.get().getEmail());
            subject = new OauthSub(OauthSubNamespace.USER, userInfo.getUserId().toString());
            return new JwtBuilder()
                    .exp(Constants.TOKEN_EXPIRY_DURATION_SECONDS)
                    .sub(subject)
                    .aud(scopes.getAud())
                    .scp(scopes.getScp())
                    .build()
                    .refresh(refreshService.issue(subject, scopes.getAud(), scopes.getScp()))
                    .sign(signer)
                    .additional("readme_token", readme.sign(userInfo))
                    .toResponse();
        } catch (JOSEException | JsonProcessingException e) {
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "Issue with JWT construction",
                    null
            ), e);
        }
    }

    private boolean sendEmail(String email, String code) {
        if(!EmailValidator.getInstance().isValid(email))
            throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                    .message("Invalid email")
                    .exception();

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
            return B64F.encode(md.digest(deviceId.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("One-time Password (OTP) failed")
                    .detail("Issue with SHA256")
                    .cause(e)
                    .exception();
        }
    }

    private String randomB64(int len) {
        try {
            byte[] bytes = new byte[len];
            SecureRandom.getInstanceStrong().nextBytes(bytes);
            return B64F.encode(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("One-time Password (OTP) failed")
                    .detail("Issue with SecureRandom generation")
                    .cause(e)
                    .exception();
        }
    }

    private String randomAlphanumeric(int len) {
        try {
            int[] ints = SecureRandom.getInstanceStrong().ints(len, 0, 36).toArray();
            StringBuilder result = new StringBuilder();
            Arrays.stream(ints).forEach(i -> result.append(Integer.toString(i, 36)));
            return result.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("One-time Password (OTP) failed")
                    .detail("Issue with SecureRandom generation")
                    .cause(e)
                    .exception();
        }
    }
}
