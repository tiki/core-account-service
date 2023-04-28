/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth;

import com.mytiki.l0_auth.features.latest.app_info.AppInfoAO;
import com.mytiki.l0_auth.features.latest.app_info.AppInfoService;
import com.mytiki.l0_auth.features.latest.org_info.OrgInfoAO;
import com.mytiki.l0_auth.features.latest.org_info.OrgInfoDO;
import com.mytiki.l0_auth.features.latest.org_info.OrgInfoRepository;
import com.mytiki.l0_auth.features.latest.org_info.OrgInfoService;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoAO;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoService;
import com.mytiki.l0_auth.main.App;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrgInfoTest {

    @Autowired
    private OrgInfoService service;

    @Autowired
    private OrgInfoRepository repository;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private AppInfoService appInfoService;

    @Test
    public void Test_Create_Success() {
        OrgInfoDO created = service.create();

        assertNull(created.getBillingId());
        assertNotNull(created.getModified());
        assertNotNull(created.getCreated());
        assertNotNull(created.getOrgId());
        assertNotNull(created.getId());
    }

    @Test
    public void Test_Get_Success() {
        UserInfoAO user = userInfoService.createIfNotExists(UUID.randomUUID() + "@test.com");
        OrgInfoAO org = service.get(user.getUserId(), user.getOrgId());

        assertTrue(org.getUsers().contains(user.getUserId()));
        assertEquals(user.getOrgId(), org.getOrgId());
        assertNull(org.getBillingId());
        assertNotNull(org.getModified());
        assertNotNull(org.getCreated());
    }

    @Test
    public void Test_Get_None_Success() {
        String orgId = UUID.randomUUID().toString();
        OrgInfoAO org = service.get(UUID.randomUUID().toString(), orgId);

        assertEquals(orgId, org.getOrgId());
        assertNull(org.getApps());
        assertNull(org.getUsers());
        assertNull(org.getBillingId());
        assertNull(org.getCreated());
        assertNull(org.getModified());
    }

    @Test
    public void Test_GetByApp_Success() {
        UserInfoAO user = userInfoService.createIfNotExists(UUID.randomUUID() + "@test.com");
        AppInfoAO app = appInfoService.create(UUID.randomUUID().toString(), user.getUserId());
        OrgInfoAO org = service.getByApp(app.getAppId());

        assertTrue(org.getUsers().contains(user.getUserId()));
        assertEquals(user.getOrgId(), org.getOrgId());
        assertNull(org.getBillingId());
        assertNotNull(org.getModified());
        assertNotNull(org.getCreated());
    }
}
