/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.cleanroom.CleanroomDO;
import com.mytiki.account.features.latest.subscription.SubscriptionDO;
import com.mytiki.account.features.latest.subscription.SubscriptionStatus;
import com.mytiki.account.utilities.error.ApiException;
import com.mytiki.account.utilities.facade.StripeF;
import com.stripe.exception.StripeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.*;

@XRayEnabled
public class OceanService {
    protected static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final OceanAws aws;
    private final String bucket;
    private final ObjectMapper mapper;
    private final OceanRepository repository;
    private final StripeF stripe;

    public OceanService(
            OceanAws aws,
            String bucket,
            ObjectMapper mapper,
            OceanRepository repository,
            StripeF stripe) {
        this.aws = aws;
        this.bucket = bucket;
        this.mapper = mapper;
        this.repository = repository;
        this.stripe = stripe;
    }

    public OceanDO count(String query) {
        return request(OceanType.COUNT, OceanQuery.wrapCount(query));
    }

    public OceanDO sample(String query) {
        return request(OceanType.SAMPLE, OceanQuery.wrapSample(query));
    }

    public OceanDO createDatabase(String cleanroomId) {
        return request(OceanType.CREATE_DATABASE, OceanQuery.createDatabase(cleanroomId));
    }

    public OceanDO dropDatabase(String cleanroomId) {
        return request(OceanType.DROP_DATABASE, OceanQuery.dropDatabase(cleanroomId));
    }

    public OceanDO ctas(String cleanroomId, String table, String query) {
        return request(OceanType.CREATE_TABLE, OceanQuery.wrapCreate(query, bucket, cleanroomId, table));
    }

    public OceanDO get(UUID requestId) {
        Optional<OceanDO> found = repository.findByRequestId(requestId);
        if(found.isPresent()) {
            if(found.get().getStatus() == OceanStatus.PENDING){
                OceanDO update = found.get();
                update.setStatus(aws.status(update.getExecutionArn()));
                update.setModified(ZonedDateTime.now());
                return repository.save(update);
            }else return found.get();
        }else return null;
    }

    public void callback(OceanAOReq req) {
        UUID requestId = UUID.fromString(req.getRequestId());
        Optional<OceanDO> found = repository.findByRequestId(requestId);
        if(found.isPresent()){
            OceanDO ocean = found.get();
            ocean.setStatus(OceanStatus.SUCCESS);
            ocean.setResultUri(req.getResultUri());
            switch(found.get().getType()) {
                case COUNT, SAMPLE -> {
                    if(req.getResultUri() != null) {
                        try {
                            List<String[]> res = aws.fetch(req.getResultUri());
                            ocean.setResult(mapper.writeValueAsString(res));
                            SubscriptionDO subscription = ocean.getSubscription();
                            if(found.get().getType() == OceanType.COUNT &&
                                    subscription.getStatus() == SubscriptionStatus.SUBSCRIBED) {
                                stripe.usage(
                                        subscription.getCleanroom().getOrg().getBillingId(),
                                        Long.parseLong(res.get(1)[0])
                                );
                            }
                        } catch (ApiException | JsonProcessingException e) {
                            logger.warn("Failed to retrieve results. Skipping", e);
                        } catch (StripeException e) {
                            logger.error("Failed to report usage to billing", e);
                        }
                    }
                }
                case CREATE_TABLE, UPDATE_TABLE -> {
                    SubscriptionDO sub = ocean.getSubscription();
                    if(sub != null) {
                        CleanroomDO cleanroom = sub.getCleanroom();
                        String table = OceanQuery.table(cleanroom.getCleanroomId().toString(), sub.getName());
                        request(OceanType.COUNT, OceanQuery.count(table));
                        request(OceanType.SAMPLE, OceanQuery.sample(table));
                    }else {
                        logger.warn("Skipping. No subscription: " + ocean.getRequestId());
                    }
                }
                case CREATE_DATABASE, DROP_DATABASE -> {}
            }
            ocean.setModified(ZonedDateTime.now());
            repository.save(ocean);
        }else {
            logger.warn("Skipping. Invalid request id: " + req.getRequestId());
        }
    }

    public List<String[]> deserializeResult(String result) {
        try {
            TypeReference<List<String[]>> typeRef = new TypeReference<>() {};
            return mapper.readValue(result, typeRef);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to read results. Skipping", e);
            return new ArrayList<>();
        }
    }

    private OceanDO request(OceanType type, String query) {
        UUID requestId = UUID.randomUUID();
        String executionArn = aws.execute(requestId, query);
        OceanDO ocean = new OceanDO();
        ZonedDateTime now = ZonedDateTime.now();
        ocean.setRequestId(requestId);
        ocean.setType(type);
        ocean.setStatus(OceanStatus.PENDING);
        ocean.setExecutionArn(executionArn);
        ocean.setCreated(now);
        ocean.setModified(now);
        return repository.save(ocean);
    }
}
