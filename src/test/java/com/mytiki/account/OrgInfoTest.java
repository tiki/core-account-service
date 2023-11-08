/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;

import com.mytiki.account.features.latest.org_info.OrgInfoAO;
import com.mytiki.account.features.latest.org_info.OrgInfoDO;
import com.mytiki.account.features.latest.org_info.OrgInfoService;
import com.mytiki.account.features.latest.user_info.UserInfoAO;
import com.mytiki.account.features.latest.user_info.UserInfoDO;
import com.mytiki.account.features.latest.user_info.UserInfoService;
import com.mytiki.account.main.App;
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
    private UserInfoService userInfoService;

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
        UserInfoDO user = userInfoService.createIfNotExists(UUID.randomUUID() + "@test.com");
        OrgInfoAO org = service.getByUser(user.getUserId().toString());

        assertTrue(org.getUsers().contains(user.getUserId().toString()));
        assertEquals(user.getOrg().getOrgId().toString(), org.getOrgId());
        assertNull(org.getBillingId());
        assertNotNull(org.getModified());
        assertNotNull(org.getCreated());
    }

    @Test
    public void Test_Get_None_Success() {
        OrgInfoAO org = service.getByUser(UUID.randomUUID().toString());
        assertNull(org);
    }
}
