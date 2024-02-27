/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.cleanroom.*;
import com.mytiki.account.features.latest.event.EventRepository;
import com.mytiki.account.features.latest.event.EventService;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.oauth.OauthSubNamespace;
import com.mytiki.account.features.latest.org.OrgRepository;
import com.mytiki.account.features.latest.org.OrgService;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileRepository;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.main.App;
import com.mytiki.account.mocks.StripeMock;
import com.mytiki.account.utilities.facade.StripeF;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CleanroomTest {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private CleanroomRepository repository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private ObjectMapper mapper;
    private CleanroomService service;
    private String cleanroomId;
    private String userId;
    @Autowired
    private OrgRepository orgRepository;
    private OrgService orgService;

    @BeforeAll
    public void before() throws StripeException {
        StripeF stripe = StripeMock.facade();
        EventService eventService = new EventService(new HashMap<>(){{}}, eventRepository, "dummy", mapper);
        service = new CleanroomService(repository, profileService, eventService);
        orgService = new OrgService(orgRepository, stripe);
    }

    @Test
    @Order(1)
    public void Test_Create_Success() {
        ProfileDO testUser = new ProfileDO();
        testUser.setEmail("test+" + UUID.randomUUID() + "@test.com");
        testUser.setUserId(UUID.randomUUID());
        testUser.setCreated(ZonedDateTime.now());
        testUser.setModified(ZonedDateTime.now());
        testUser.setOrg(orgService.create("dummy@dummy.com"));
        testUser = profileRepository.save(testUser);
        userId = testUser.getUserId().toString();

        CleanroomAOReq req = new CleanroomAOReq(null, "dummy");
        CleanroomAORsp cleanroom = service.create(req,
                new OauthSub(OauthSubNamespace.USER, testUser.getUserId().toString()));
        cleanroomId = cleanroom.getCleanroomId();

        assertNotNull(cleanroom.getName());
        assertNotNull(cleanroom.getCleanroomId());
        assertNotNull(cleanroom.getModified());
        assertNotNull(cleanroom.getCreated());
        assertEquals(testUser.getOrg().getOrgId().toString(), cleanroom.getOrgId());
        assertEquals(cleanroom.getAws(), req.getAws());
    }

    @Test
    @Order(2)
    public void Test_Get_Success() {
        CleanroomAORsp cleanroom = service.get(new OauthSub(OauthSubNamespace.USER, userId), cleanroomId);
        assertEquals(cleanroomId, cleanroom.getCleanroomId());
        assertEquals("dummy", cleanroom.getAws());
        assertNotNull(cleanroom.getCleanroomId());
        assertNotNull(cleanroom.getModified());
        assertNotNull(cleanroom.getCreated());
    }

    @Test
    @Order(3)
    public void Test_List_Success() {
        List<CleanroomAO> cleanrooms = service.list(new OauthSub(OauthSubNamespace.USER, userId));
        assertEquals(1, cleanrooms.size());
        assertEquals(cleanroomId, cleanrooms.get(0).getCleanroomId());
    }
}
