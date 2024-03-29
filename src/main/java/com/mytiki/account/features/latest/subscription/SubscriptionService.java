/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import com.mytiki.account.features.latest.cleanroom.CleanroomDO;
import com.mytiki.account.features.latest.cleanroom.CleanroomService;
import com.mytiki.account.features.latest.event.EventDO;
import com.mytiki.account.features.latest.event.EventService;
import com.mytiki.account.features.latest.event.ao.EventAOSubPurchaseRsp;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import com.mytiki.account.utilities.facade.StripeF;
import com.stripe.exception.StripeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class SubscriptionService {
    protected static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SubscriptionRepository repository;
    private final EventService eventService;
    private final CleanroomService cleanroomService;
    private final StripeF stripe;

    public SubscriptionService(
            SubscriptionRepository repository,
            EventService eventService,
            CleanroomService cleanroomService,
            StripeF stripe) {
        this.repository = repository;
        this.eventService = eventService;
        this.cleanroomService = cleanroomService;
        this.stripe = stripe;
    }

    public List<SubscriptionAO> list(OauthSub sub, String status) {
        if(!sub.isUser()) throw new ErrorBuilder(HttpStatus.FORBIDDEN).exception();
        UUID userId = UUID.fromString(sub.getId());
        List<SubscriptionDO> subscriptions = status != null ?
                repository.findByStatusAndUserId(SubscriptionStatus.fromString(status), userId):
                repository.findByUserId(userId);
        return subscriptions.stream().map((subscription) -> {
            SubscriptionAO rsp = new SubscriptionAO();
            rsp.setCreated(subscription.getCreated());
            rsp.setModified(subscription.getModified());
            rsp.setSubscriptionId(subscription.getSubscriptionId().toString());
            rsp.setCleanroomId(subscription.getCleanroom().getCleanroomId().toString());
            rsp.setStatus(subscription.getStatus().toString());
            rsp.setName(subscription.getName());
            return rsp;
        }).collect(Collectors.toList());
    }

    public SubscriptionAORsp get(OauthSub sub, String subscriptionId) {
        Optional<SubscriptionDO> found = repository.findBySubscriptionId(UUID.fromString(subscriptionId));
        if(found.isEmpty()) throw new ErrorBuilder(HttpStatus.NOT_FOUND).exception();
        SubscriptionDO subscription = found.get();
        cleanroomService.guard(sub, subscription.getCleanroom().getCleanroomId().toString());
        return toAORsp(subscription);
    }

    public SubscriptionAORsp estimate(OauthSub sub, SubscriptionAOReq req) {
        CleanroomDO cleanroom = cleanroomService.guard(sub, req.getCleanroomId());
        SubscriptionDO subscription = new SubscriptionDO();
        subscription.setSubscriptionId(UUID.randomUUID());
        String query = req.getQuery().stripTrailing();
        subscription.setQuery(query.endsWith(";") ? query.substring(0, query.length()-1) : query);
        subscription.setStatus(SubscriptionStatus.ESTIMATE);
        subscription.setName(req.getName());
        subscription.setCleanroom(cleanroom);
        ZonedDateTime now = ZonedDateTime.now();
        subscription.setCreated(now);
        subscription.setModified(now);
        EventDO event = eventService.createEstimate(subscription);
        subscription.setEvents(List.of(event));
        SubscriptionDO saved = repository.save(subscription);
        return toAORsp(saved);
    }

    public SubscriptionAORsp purchase(OauthSub sub, String subscriptionId) {
        Optional<SubscriptionDO> found = repository.findBySubscriptionId(UUID.fromString(subscriptionId));
        if(found.isEmpty()) throw new ErrorBuilder(HttpStatus.NOT_FOUND).exception();
        if(found.get().getStatus() != SubscriptionStatus.ESTIMATE)
            throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                    .message("Subscription exists")
                    .help("Create a new estimate")
                    .exception();
        boolean hasBilling = false;
        try{ hasBilling = stripe.isValid(found.get().getCleanroom().getOrg().getBillingId()); }
        catch (StripeException e) { logger.error("Stripe error", e); }
        if(!hasBilling) throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                .message("Request requires a valid billing profile")
                .help("Go to: https://billing.mytiki.com/p/login/3cs2afdmE27veD6288")
                .exception();
        cleanroomService.guard(sub, found.get().getCleanroom().getCleanroomId().toString());
        SubscriptionDO update = found.get();
        EventDO event = eventService.createPurchase(update);
        update.setStatus(SubscriptionStatus.SUBSCRIBED);
        List<EventDO> events = update.getEvents() != null ? new ArrayList<>(update.getEvents()) : new ArrayList<>();
        events.add(event);
        update.setEvents(events);
        update.setModified(ZonedDateTime.now());
        SubscriptionDO saved = repository.save(update);
        return toAORsp(saved);
    }

    public void callback(EventAOSubPurchaseRsp rsp) {
        UUID requestId = UUID.fromString(rsp.getRequestId());
        List<SubscriptionDO> events = repository.findByEventsRequestId(requestId);
        if(events.size() != 1)
            throw new ErrorBuilder(HttpStatus.NOT_FOUND)
                    .message("Issue identifying subscription, multiple or none mapped.")
                    .properties("requestId", rsp.getRequestId())
                    .exception();
        SubscriptionDO subs = events.get(0);
        try {
            stripe.usage(subs.getCleanroom().getOrg().getBillingId(), rsp.getCount());
        }catch (StripeException e) {
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message(e.getMessage())
                    .properties("requestId", rsp.getRequestId())
                    .exception();
        }
    }

    private SubscriptionAORsp toAORsp(SubscriptionDO src) {
        SubscriptionAORsp rsp = new SubscriptionAORsp();
        rsp.setCreated(src.getCreated());
        rsp.setModified(src.getModified());
        rsp.setSubscriptionId(src.getSubscriptionId().toString());
        rsp.setCleanroomId(src.getCleanroom().getCleanroomId().toString());
        rsp.setQuery(src.getQuery());
        rsp.setStatus(src.getStatus().toString());
        rsp.setName(src.getName());
        rsp.setEvents(src.getEvents().stream().map(eventService::toAORsp).collect(Collectors.toList()));
        return rsp;
    }
}
