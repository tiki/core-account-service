/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth;

import com.mytiki.l0_auth.features.latest.app_info.AppInfoDO;
import com.mytiki.l0_auth.features.latest.app_info.AppInfoRepository;
import com.mytiki.l0_auth.features.latest.app_info.AppInfoService;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoDO;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoRepository;
import com.mytiki.l0_auth.main.App;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
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
public class AppInfoTest {

    @Autowired
    private AppInfoRepository repository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private AppInfoService service;

    @Test
    public void Test_Create_Success() {
        String name = "testApp";

        UserInfoDO testUser = new UserInfoDO();
        testUser.setEmail("test+" + UUID.randomUUID().toString() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser = userInfoRepository.save(testUser);

        AppInfoDO app = service.create(name, testUser);
        assertEquals(name, app.getName());
        assertNotNull(app.getId());
        assertNotNull(app.getAppId());
        assertNotNull(app.getCreated());
        assertNotNull(app.getModified());
        assertFalse(app.getUsers().isEmpty());
        assertEquals(1, app.getUsers().size());
        assertEquals(testUser.getId(), app.getUsers().stream().findFirst().get().getId());
    }

    @Test
    public void Test_Create_NoUser_Failure() {
        String name = "testApp";

        UserInfoDO testUser = new UserInfoDO();
        testUser.setEmail("test+" + UUID.randomUUID().toString() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());

        InvalidDataAccessApiUsageException ex = assertThrows(InvalidDataAccessApiUsageException.class,
                () -> service.create(name, testUser));
        assertNotNull(ex);
    }
}
