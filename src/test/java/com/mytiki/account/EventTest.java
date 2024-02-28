/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.cleanroom.CleanroomDO;
import com.mytiki.account.features.latest.cleanroom.CleanroomRepository;
import com.mytiki.account.features.latest.cleanroom.CleanroomService;
import com.mytiki.account.features.latest.event.*;
import com.mytiki.account.features.latest.event.ao.EventAOErrorRsp;
import com.mytiki.account.features.latest.event.ao.EventAOSubEstimateRsp;
import com.mytiki.account.features.latest.event.ao.EventAOSubEstimateRspCol;
import com.mytiki.account.features.latest.event.ao.EventAOSubPurchaseRsp;
import com.mytiki.account.features.latest.event.status.EventStatus;
import com.mytiki.account.features.latest.event.type.EventType;
import com.mytiki.account.features.latest.org.OrgDO;
import com.mytiki.account.features.latest.org.OrgRepository;
import com.mytiki.account.features.latest.subscription.SubscriptionDO;
import com.mytiki.account.features.latest.subscription.SubscriptionRepository;
import com.mytiki.account.features.latest.subscription.SubscriptionService;
import com.mytiki.account.features.latest.subscription.SubscriptionStatus;
import com.mytiki.account.main.App;
import com.mytiki.account.mocks.JwtMock;
import com.mytiki.account.mocks.SfnMock;
import com.mytiki.account.mocks.StripeMock;
import com.mytiki.account.utilities.facade.StripeF;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.sfn.SfnClient;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class, JwtMock.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private EventRepository repository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private CleanroomService cleanroomService;
    @Autowired
    private CleanroomRepository cleanroomRepository;
    @Autowired
    private OrgRepository orgRepository;
    private EventHandler handler;
    private EventService service;

    @BeforeEach
    public void before() throws StripeException {
        SfnClient client = SfnMock.mock("dummy-execution-arn");
        this.service = new EventService(new HashMap<>(){{}}, repository, client, mapper);
        StripeF stripe = StripeMock.facade();
        SubscriptionService subscriptionService = new SubscriptionService(subscriptionRepository, this.service, cleanroomService, stripe);
        this.handler = new EventHandler(repository, mapper, subscriptionService);
    }

    @Test
    public void Test_Create_Cleanroom_Success(){
        CleanroomDO cleanroom = new CleanroomDO();
        cleanroom.setCleanroomId(UUID.randomUUID());
        cleanroom.setAws("dummy");
        cleanroom.setName("dummy");

        EventDO event = service.createCleanroom(cleanroom);
        assertEquals(EventType.CREATE_CLEANROOM, event.getType());
        assertEquals(EventStatus.PENDING, event.getStatus());
        assertNull(event.getResult());
        assertNotNull(event.getCreated());
        assertNotNull(event.getModified());
        assertNotNull(event.getRequestId());
        assertNotNull(event.getId());
    }

    @Test
    public void Test_Subscription_Estimate_Success(){
        SubscriptionDO subscription = new SubscriptionDO();
        subscription.setQuery("dummy");

        EventDO event = service.createEstimate(subscription);
        assertEquals(EventType.ESTIMATE_SUBSCRIPTION, event.getType());
        assertEquals(EventStatus.PENDING, event.getStatus());
        assertNull(event.getResult());
        assertNotNull(event.getCreated());
        assertNotNull(event.getModified());
        assertNotNull(event.getRequestId());
        assertNotNull(event.getId());
    }

    @Test
    public void Test_Subscription_Purchase_Success(){
        SubscriptionDO subscription = new SubscriptionDO();
        subscription.setQuery("dummy");

        EventDO event = service.createPurchase(subscription);
        assertEquals(EventType.PURCHASE_SUBSCRIPTION, event.getType());
        assertEquals(EventStatus.PENDING, event.getStatus());
        assertNull(event.getResult());
        assertNotNull(event.getCreated());
        assertNotNull(event.getModified());
        assertNotNull(event.getRequestId());
        assertNotNull(event.getId());
    }

    @Test
    public void Test_Create_Cleanroom_Callback(){
        SubscriptionDO subscription = new SubscriptionDO();
        subscription.setQuery("dummy");

        EventDO event = service.createEstimate(subscription);
        EventAOSubEstimateRsp rsp = new EventAOSubEstimateRsp();
        rsp.setRequestId(event.getRequestId().toString());
        handler.process(EventType.CREATE_CLEANROOM, rsp);

        Optional<EventDO> found = repository.findByRequestId(event.getRequestId());
        assertTrue(found.isPresent());
        assertEquals(EventStatus.SUCCESS, found.get().getStatus());
    }

    @Test
    public void Test_Subscription_Estimate_Callback(){
        SubscriptionDO subscription = new SubscriptionDO();
        subscription.setQuery("dummy");

        EventDO event = service.createEstimate(subscription);
        EventAOSubEstimateRsp rsp = new EventAOSubEstimateRsp();
        rsp.setRequestId(event.getRequestId().toString());
        rsp.setCount(1L);

        EventAOSubEstimateRspCol col = new EventAOSubEstimateRspCol();
        col.setValue("dummy");
        rsp.setSample(List.of(List.of(col)));
        handler.process(EventType.ESTIMATE_SUBSCRIPTION, rsp);

        Optional<EventDO> found = repository.findByRequestId(event.getRequestId());
        assertTrue(found.isPresent());
        assertEquals(EventStatus.SUCCESS, found.get().getStatus());

        EventAOSubEstimateRsp result = (EventAOSubEstimateRsp) service.toAORsp(found.get()).getResult();
        assertEquals(1L, result.getCount());
        assertEquals("dummy", result.getSample().get(0).get(0).getValue());
    }

    @Test
    public void Test_Subscription_Purchase_Callback(){
        OrgDO org = new OrgDO();
        org.setOrgId(UUID.randomUUID());
        org.setModified(ZonedDateTime.now());
        org.setCreated(ZonedDateTime.now());
        org.setBillingId("dummy");
        org = orgRepository.save(org);

        CleanroomDO cleanroom = new CleanroomDO();
        cleanroom.setName("dummy");
        cleanroom.setModified(ZonedDateTime.now());
        cleanroom.setCreated(ZonedDateTime.now());
        cleanroom.setAws("dummy");
        cleanroom.setDescription("dummy");
        cleanroom.setCleanroomId(UUID.randomUUID());
        cleanroom.setOrg(org);
        cleanroom = cleanroomRepository.save(cleanroom);

        SubscriptionDO subscription = new SubscriptionDO();
        subscription.setQuery("dummy");
        subscription.setSubscriptionId(UUID.randomUUID());
        subscription.setName("dummy");
        subscription.setStatus(SubscriptionStatus.ESTIMATE);
        subscription.setModified(ZonedDateTime.now());
        subscription.setCreated(ZonedDateTime.now());
        subscription.setCleanroom(cleanroom);
        EventDO event = service.createPurchase(subscription);

        subscription.setEvents(List.of(event));
        subscriptionRepository.save(subscription);

        EventAOSubPurchaseRsp rsp = new EventAOSubPurchaseRsp();
        rsp.setRequestId(event.getRequestId().toString());
        rsp.setCount(1L);
        handler.process(EventType.PURCHASE_SUBSCRIPTION, rsp);

        Optional<EventDO> found = repository.findByRequestId(event.getRequestId());
        assertTrue(found.isPresent());
        assertEquals(EventStatus.SUCCESS, found.get().getStatus());

        EventAOSubPurchaseRsp result = (EventAOSubPurchaseRsp) service.toAORsp(found.get()).getResult();
        assertEquals(1L, result.getCount());
    }

    @Test
    public void Test_Error_Callback(){
        CleanroomDO cleanroom = new CleanroomDO();
        cleanroom.setCleanroomId(UUID.randomUUID());
        cleanroom.setAws("dummy");
        cleanroom.setName("dummy");

        EventDO event = service.createCleanroom(cleanroom);

        EventAOErrorRsp rsp = new EventAOErrorRsp();
        rsp.setRequestId(event.getRequestId().toString());
        rsp.setCause("dummy cause");
        rsp.setMessage("dummy message");
        handler.error(rsp);

        Optional<EventDO> found = repository.findByRequestId(event.getRequestId());
        assertTrue(found.isPresent());
        assertEquals(EventStatus.FAILED, found.get().getStatus());

        EventAOErrorRsp result = (EventAOErrorRsp) service.toAORsp(found.get()).getResult();
        assertEquals("dummy cause", result.getCause());
        assertEquals("dummy message", result.getMessage());
    }
}
