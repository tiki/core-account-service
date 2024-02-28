/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.event.ao.EventAOBase;
import com.mytiki.account.features.latest.event.ao.EventAOErrorRsp;
import com.mytiki.account.features.latest.event.ao.EventAOSubPurchaseRsp;
import com.mytiki.account.features.latest.event.status.EventStatus;
import com.mytiki.account.features.latest.event.type.EventType;
import com.mytiki.account.features.latest.subscription.SubscriptionService;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.UUID;

public class EventHandler {
    protected static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EventRepository repository;
    private final ObjectMapper mapper;
    private final SubscriptionService subscriptionService;

    public EventHandler(EventRepository repository, ObjectMapper mapper, SubscriptionService subscriptionService) {
        this.repository = repository;
        this.mapper = mapper;
        this.subscriptionService = subscriptionService;
    }

    public void process(EventType type, EventAOBase rsp) {
        switch (type) {
            case PURCHASE_SUBSCRIPTION -> subscriptionService.callback((EventAOSubPurchaseRsp) rsp);
            default -> {}
        };
        save(EventStatus.SUCCESS, rsp);
    }

    public void error(EventAOErrorRsp rsp) {
        logger.warn("Event error: {}", rsp.toString());
        save(EventStatus.FAILED, rsp);
    }

    private void save(EventStatus status, EventAOBase rsp) {
        UUID requestId = UUID.fromString(rsp.getRequestId());
        Optional<EventDO> found = repository.findByRequestId(requestId);
        if(found.isEmpty()) throw new ErrorBuilder(HttpStatus.NOT_FOUND)
                .message("Invalid requestId")
                .properties("requestId", requestId.toString())
                .exception();
        else {
            EventDO update = found.get();
            try{
                String res = mapper.writeValueAsString(rsp);
                update.setStatus(status);
                update.setResult(res);
                repository.save(update);
            } catch (JsonProcessingException e) {
                update.setStatus(EventStatus.FAILED);
                repository.save(update);
                throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                        .message(e.getMessage())
                        .properties("requestId", requestId.toString())
                        .cause(e)
                        .exception();
            }
        }
    }
}
