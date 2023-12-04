/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import com.mytiki.account.features.latest.cleanroom.CleanroomDO;
import com.mytiki.account.features.latest.cleanroom.CleanroomService;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.ocean.OceanDO;
import com.mytiki.account.features.latest.ocean.OceanService;
import com.mytiki.account.features.latest.ocean.OceanStatus;
import com.mytiki.account.features.latest.ocean.OceanType;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SubscriptionService {
    private final SubscriptionRepository repository;
    private final OceanService oceanService;
    private final CleanroomService cleanroomService;

    public SubscriptionService(
            SubscriptionRepository repository,
            OceanService oceanService,
            CleanroomService cleanroomService) {
        this.repository = repository;
        this.oceanService = oceanService;
        this.cleanroomService = cleanroomService;
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

        List<OceanDO> results = subscription.getResults();
        if(results != null){
            List<OceanDO> updated = new ArrayList<>(results.size());
            results.forEach((res) -> {
                if(res.getStatus() == OceanStatus.PENDING) updated.add(oceanService.get(res.getRequestId()));
                else updated.add(res);
            });
            subscription.setResults(updated);
        }

        return toAORsp(subscription);
    }

    public SubscriptionAORsp estimate(OauthSub sub, SubscriptionAOReq req) {
        CleanroomDO cleanroom = cleanroomService.guard(sub, req.getCleanroomId());
        SubscriptionDO subscription = new SubscriptionDO();
        subscription.setSubscriptionId(UUID.randomUUID());
        subscription.setQuery(req.getQuery());
        subscription.setStatus(SubscriptionStatus.ESTIMATE);
        subscription.setName(req.getName());
        subscription.setCleanroom(cleanroom);
        OceanDO res1 = oceanService.count(req.getQuery());
        OceanDO res2 = oceanService.sample(req.getQuery());
        subscription.setResults(List.of(res1, res2));
        ZonedDateTime now = ZonedDateTime.now();
        subscription.setCreated(now);
        subscription.setModified(now);
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
        cleanroomService.guard(sub, found.get().getCleanroom().getCleanroomId().toString());
        SubscriptionDO update = found.get();
        update.setStatus(SubscriptionStatus.SUBSCRIBED);
        OceanDO res = oceanService.ctas(
                update.getCleanroom().getCleanroomId().toString(), update.getName(), update.getQuery());
        List<OceanDO> results = update.getResults() != null ? new ArrayList<>(update.getResults()) : new ArrayList<>();
        results.add(res);
        update.setResults(results);
        update.setModified(ZonedDateTime.now());
        SubscriptionDO saved = repository.save(update);
        return toAORsp(saved);
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

        List<SubscriptionAORspCount> countList = new ArrayList<>();
        List<SubscriptionAORspSample> sampleList = new ArrayList<>();

        if(src.getResults() != null){
            src.getResults().forEach((res) -> {
                switch (res.getType()){
                    case SAMPLE -> {
                        List<String[]> result = oceanService.deserializeResult(res.getResult());
                        if(result != null && !result.isEmpty()){
                            SubscriptionAORspSample sample = new SubscriptionAORspSample();
                            sample.setCreated(res.getCreated());
                            sample.setModified(res.getModified());
                            sample.setStatus(res.getStatus().toString());
                            sample.setRecords(result.stream()
                                    .map((val) -> String.join(",", val)).collect(Collectors.toList()));
                            sampleList.add(sample);
                        }
                    }
                    case COUNT -> {
                        List<String[]> result = oceanService.deserializeResult(res.getResult());
                        if(result != null && !result.isEmpty()){
                            SubscriptionAORspCount count = new SubscriptionAORspCount();
                            count.setCreated(res.getCreated());
                            count.setModified(res.getModified());
                            count.setStatus(res.getStatus().toString());
                            count.setTotal(Long.parseLong(result.get(1)[0]));
                            countList.add(count);
                        }
                    }
                    default -> {}
                }
            });
        }

        rsp.setCount(countList);
        rsp.setSample(sampleList);
        return rsp;
    }

}
