/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;

import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.oauth.OauthSubNamespace;
import com.mytiki.account.features.latest.org.OrgAO;
import com.mytiki.account.features.latest.org.OrgDO;
import com.mytiki.account.features.latest.org.OrgRepository;
import com.mytiki.account.features.latest.org.OrgService;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.main.App;
import com.mytiki.account.mocks.StripeMock;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.*;
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
public class OrgTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private OrgRepository repository;

    private OrgService service;

    @BeforeAll
    public void before() throws StripeException {
        service = new OrgService(repository, StripeMock.facade());
    }

    @Test
    public void Test_Create_Success() {
        OrgDO created = service.create("dummy@dummy.com");
        assertNotNull(created.getBillingId());
        assertNotNull(created.getModified());
        assertNotNull(created.getCreated());
        assertNotNull(created.getOrgId());
        assertNotNull(created.getId());
    }

    @Test
    public void Test_Get_Success() {
        ProfileDO user = profileService.createIfNotExists(UUID.randomUUID() + "@test.com");
        OrgAO org = service.get(new OauthSub(OauthSubNamespace.USER, user.getUserId().toString()));

        assertTrue(org.getUsers().contains(user.getUserId().toString()));
        assertEquals(user.getOrg().getOrgId().toString(), org.getOrgId());
        assertNotNull(org.getBillingId());
        assertNotNull(org.getModified());
        assertNotNull(org.getCreated());
    }

    @Test
    public void Test_Get_None_Success() {
        OrgAO org = service.get(new OauthSub(OauthSubNamespace.USER, UUID.randomUUID().toString()));
        assertNull(org);
    }
}
