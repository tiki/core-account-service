/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth;

import com.mytiki.l0_auth.features.latest.user_info.*;
import com.mytiki.l0_auth.main.App;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserInfoTest {

    @Autowired
    private UserInfoRepository repository;

    @Autowired
    private UserInfoService service;

    @Test
    public void Test_Get_Success() throws JOSEException {
        UserInfoDO saved = new UserInfoDO();
        saved.setEmail("test+" + UUID.randomUUID() + "@test.com");
        saved.setUserId(UUID.randomUUID());
        saved.setCreated(ZonedDateTime.now());
        saved.setModified(ZonedDateTime.now());
        saved = repository.save(saved);

        UserInfoAO user = service.get(saved.getUserId().toString());
        assertEquals(saved.getEmail(), user.getEmail());
        assertEquals(saved.getModified().withNano(0), user.getUpdatedAt().withNano(0));
        assertEquals(0, user.getApps().size());
        assertEquals(saved.getUserId().toString(), user.getSub());
    }

    @Test
    public void Test_Get_NoUser_Success() throws JOSEException {
        String sub = UUID.randomUUID().toString();
        UserInfoAO user = service.get(sub);
        assertNull(user.getEmail());
        assertNull(user.getUpdatedAt());
        assertNull(user.getApps());
        assertEquals(sub, user.getSub());
    }

    @Test
    public void Test_Update_Success() throws JOSEException {
        UserInfoDO saved = new UserInfoDO();
        saved.setEmail("test+" + UUID.randomUUID() + "@test.com");
        saved.setUserId(UUID.randomUUID());
        saved.setCreated(ZonedDateTime.now());
        saved.setModified(ZonedDateTime.now());
        saved = repository.save(saved);

        UserInfoAOUpdate update = new UserInfoAOUpdate();
        update.setEmail(saved.getEmail() + "updated");

        UserInfoAO user = service.update(saved.getUserId().toString(), update);
        assertEquals(saved.getEmail() + "updated", user.getEmail());
        assertNotEquals(saved.getModified(), user.getUpdatedAt());
    }
}
