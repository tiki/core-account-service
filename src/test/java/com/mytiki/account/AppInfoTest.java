/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;

import com.mytiki.account.features.latest.app_info.AppInfoAO;
import com.mytiki.account.features.latest.app_info.AppInfoRepository;
import com.mytiki.account.features.latest.app_info.AppInfoService;
import com.mytiki.account.features.latest.org_info.OrgInfoService;
import com.mytiki.account.features.latest.user_info.UserInfoDO;
import com.mytiki.account.features.latest.user_info.UserInfoRepository;
import com.mytiki.account.main.App;
import com.mytiki.spring_rest_api.ApiException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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

    @Autowired
    private OrgInfoService orgInfoService;

    @Test
    public void Test_Create_Success() {
        String name = "testApp";

        UserInfoDO testUser = new UserInfoDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgInfoService.create());
        testUser = userInfoRepository.save(testUser);

        AppInfoAO app = service.create(name, testUser.getUserId().toString());
        assertEquals(name, app.getName());
        assertNotNull(app.getAppId());
        assertNotNull(app.getModified());
        assertNotNull(app.getCreated());
        assertEquals(app.getOrgId(), testUser.getOrg().getOrgId().toString());
    }

    @Test
    public void Test_Create_NoUser_Failure() {
        String name = "testApp";

        UserInfoDO testUser = new UserInfoDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());

        ApiException ex = assertThrows(ApiException.class,
                () -> service.create(name, testUser.getUserId().toString()));
        assertNotNull(ex);
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
    }

    @Test
    public void Test_Get_Success() {
        String name = "testApp";

        UserInfoDO testUser = new UserInfoDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgInfoService.create());
        testUser = userInfoRepository.save(testUser);

        AppInfoAO app = service.create(name, testUser.getUserId().toString());
        AppInfoAO found = service.get(app.getAppId());

        assertEquals(app.getAppId(), found.getAppId());
        assertEquals(app.getName(), found.getName());
        assertEquals(app.getOrgId(), found.getOrgId());
    }

    @Test
    public void Test_Get_NoApp_Success() {
        String appId = UUID.randomUUID().toString();
        AppInfoAO found = service.get(appId);
        assertNull(found);
    }
}
