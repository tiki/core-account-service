/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;

import com.mytiki.account.features.latest.org.OrgService;
import com.mytiki.account.features.latest.profile.ProfileAO;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileRepository;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.main.App;
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
public class ProfileTest {

    @Autowired
    private ProfileRepository repository;

    @Autowired
    private ProfileService service;

    @Autowired
    private OrgService orgService;

    @Test
    public void Test_Get_Success() {
        ProfileDO saved = new ProfileDO();
        saved.setEmail("test+" + UUID.randomUUID() + "@test.com");
        saved.setUserId(UUID.randomUUID());
        saved.setCreated(ZonedDateTime.now());
        saved.setModified(ZonedDateTime.now());
        saved.setOrg(orgService.create());
        saved = repository.save(saved);

        ProfileAO user = service.get(saved.getUserId().toString());
        assertEquals(saved.getEmail(), user.getEmail());
        assertNotNull(user.getOrgId());
        assertEquals(saved.getUserId().toString(), user.getUserId());
    }

    @Test
    public void Test_Get_NoUser_Success() {
        String sub = UUID.randomUUID().toString();
        ProfileAO user = service.get(sub);
        assertNull(user.getEmail());
        assertNull(user.getModified());
        assertNull(user.getCreated());
        assertNull(user.getOrgId());
        assertEquals(sub, user.getUserId());
    }
}
