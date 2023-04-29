/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth;

import com.mytiki.l0_auth.features.latest.org_info.OrgInfoAO;
import com.mytiki.l0_auth.features.latest.org_info.OrgInfoService;
import com.mytiki.l0_auth.features.latest.stripe.StripeService;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoAO;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoService;
import com.mytiki.l0_auth.main.App;
import com.mytiki.l0_auth.mocks.StripeMock;
import com.mytiki.spring_rest_api.ApiException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StripeTest {

    @Autowired
    private StripeService service;

    @Value("${com.mytiki.l0_auth.stripe.signing_secret}")
    private String stripeSecret;

    @Autowired
    private OrgInfoService orgInfoService;

    @Autowired
    private UserInfoService userInfoService;

    @Test
    public void Test_Checkout_Success() throws NoSuchAlgorithmException, InvalidKeyException {
        UserInfoAO user = userInfoService.createIfNotExists(UUID.randomUUID() + "@test.com");

        String stripeId = UUID.randomUUID().toString();
        String orgId = user.getOrgId();
        String payload = StripeMock.checkoutSessionCompleted(stripeId, orgId);
        String signature = StripeMock.generateSignature(stripeSecret, payload);
        service.updateBilling(payload, signature);

        OrgInfoAO org = orgInfoService.getForUser(user.getUserId(), user.getOrgId());
        assertEquals(stripeId, org.getBillingId());
    }

    @Test
    public void Test_BadSignature_Failure(){
        ApiException ex = assertThrows(ApiException.class,
                () -> service.updateBilling(StripeMock.checkoutSessionCompleted(
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString()),
                        UUID.randomUUID().toString()));
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
    }

    @Test
    public void Test_BadEvent_Failure(){
        ApiException ex = assertThrows(ApiException.class,
                () -> {
            String payload = StripeMock.checkoutSessionCompleted(
                    UUID.randomUUID().toString(), UUID.randomUUID().toString());
            payload.replace("checkout.session.completed", "payment_intent.succeeded");
            String signature = StripeMock.generateSignature(stripeSecret, payload);
            service.updateBilling(payload, signature);
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    public void Test_BadOrg_Failure(){
        ApiException ex = assertThrows(ApiException.class,
                () -> {
                    String payload = StripeMock.checkoutSessionCompleted(
                            UUID.randomUUID().toString(), UUID.randomUUID().toString());
                    String signature = StripeMock.generateSignature(stripeSecret, payload);
                    service.updateBilling(payload, signature);
                });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }
}
