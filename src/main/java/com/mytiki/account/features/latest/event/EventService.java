/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.event;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.cleanroom.CleanroomDO;
import com.mytiki.account.features.latest.event.ao.*;
import com.mytiki.account.features.latest.event.status.EventStatus;
import com.mytiki.account.features.latest.event.type.EventType;
import com.mytiki.account.features.latest.subscription.SubscriptionDO;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.SfnException;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;
import software.amazon.awssdk.services.sfn.model.StartExecutionResponse;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

public class EventService {
    protected static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Map<String, String> arns;
    private final EventRepository repository;
    private final SfnClient sfnClient;
    private final ObjectMapper mapper;

    public EventService(
            Map<String, String> arns,
            EventRepository repository,
            String region,
            ObjectMapper mapper) {
        this(arns, repository, SfnClient.builder()
                        .region(Region.of(region))
                        .overrideConfiguration(ClientOverrideConfiguration
                                .builder()
                                .addExecutionInterceptor(new TracingInterceptor())
                                .build())
                        .build(),
                mapper);
    }

    public EventService(
            Map<String, String> arns,
            EventRepository repository,
            SfnClient sfnClient,
            ObjectMapper mapper) {
        this.arns = arns;
        this.repository = repository;
        this.sfnClient = sfnClient;
        this.mapper = mapper;
    }

    public EventDO createCleanroom(CleanroomDO cleanroom){
        UUID requestId = UUID.randomUUID();

        EventAOCrCreateReq req = new EventAOCrCreateReq();
        req.setDatabase(cleanroom.getName());
        req.setAccount(cleanroom.getAws());
        req.setRequestId(requestId.toString());
        String exec = execute(arns.get("cr_create"), req);
        logger.trace("create cleanroom: " + exec);

        EventDO db = new EventDO();
        ZonedDateTime now = ZonedDateTime.now();
        db.setStatus(EventStatus.PENDING);
        db.setType(EventType.CREATE_CLEANROOM);
        db.setRequestId(requestId);
        db.setCreated(now);
        db.setModified(now);
        return repository.save(db);
    }
    public EventDO createEstimate(SubscriptionDO subscription){
        UUID requestId = UUID.randomUUID();

        EventAOSubEstimateReq req = new EventAOSubEstimateReq();
        req.setQuery(subscription.getQuery());
        req.setRequestId(requestId.toString());
        String exec = execute(arns.get("sub_estimate"), req);
        logger.trace("create estimate: " + exec);

        EventDO db = new EventDO();
        ZonedDateTime now = ZonedDateTime.now();
        db.setStatus(EventStatus.PENDING);
        db.setType(EventType.ESTIMATE_SUBSCRIPTION);
        db.setRequestId(requestId);
        db.setCreated(now);
        db.setModified(now);
        return repository.save(db);
    }
    public EventDO createPurchase(SubscriptionDO subscription){
        UUID requestId = UUID.randomUUID();

        EventAOSubEstimateReq req = new EventAOSubEstimateReq();
        req.setQuery(subscription.getQuery());
        req.setRequestId(requestId.toString());
        String exec = execute(arns.get("sub_purchase"), req);
        logger.trace("create purchase: " + exec);

        EventDO db = new EventDO();
        ZonedDateTime now = ZonedDateTime.now();
        db.setStatus(EventStatus.PENDING);
        db.setType(EventType.PURCHASE_SUBSCRIPTION);
        db.setRequestId(requestId);
        db.setCreated(now);
        db.setModified(now);
        return repository.save(db);
    }

    public EventAORsp<? extends EventAOBase> toAORsp(EventDO event) {
        try {
            EventAORsp<EventAOBase> rsp = new EventAORsp<>();
            rsp.setCreated(event.getCreated());
            rsp.setModified(event.getModified());
            rsp.setType(event.getType().toString());
            rsp.setStatus(event.getStatus().toString());
            rsp.setRequestId(event.getRequestId().toString());
            if(event.getResult() != null) {
                rsp.setResult(event.getStatus().equals(EventStatus.FAILED) ?
                        mapper.readValue(event.getResult(), EventAOErrorRsp.class) :
                        switch (event.getType()) {
                            case CREATE_CLEANROOM -> mapper.readValue(event.getResult(), EventAOCrCreateRsp.class);
                            case ESTIMATE_SUBSCRIPTION ->
                                    mapper.readValue(event.getResult(), EventAOSubEstimateRsp.class);
                            case PURCHASE_SUBSCRIPTION ->
                                    mapper.readValue(event.getResult(), EventAOSubPurchaseRsp.class);
                        });
            }
            return rsp;
        }catch (JsonProcessingException | EnumConstantNotPresentException e){
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("Failed to parse event result: " + event.getId())
                    .cause(e)
                    .exception();
        }
    }

    private String execute(String arn, EventAOBase req) {
        try {
            StartExecutionRequest executionRequest = StartExecutionRequest.builder()
                    .input(mapper.writeValueAsString(req))
                    .stateMachineArn(arn)
                    .name(req.getRequestId())
                    .build();
            StartExecutionResponse rsp = sfnClient.startExecution(executionRequest);
            return rsp.executionArn();
        } catch (JsonProcessingException | SfnException e) {
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("Failed to start step function: " + req.getRequestId())
                    .cause(e)
                    .exception();
        }
    }
}
