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
    private final String bucket;

    public SubscriptionService(
            SubscriptionRepository repository,
            OceanService oceanService,
            CleanroomService cleanroomService,
            String bucket) {
        this.repository = repository;
        this.oceanService = oceanService;
        this.cleanroomService = cleanroomService;
        this.bucket = bucket;
    }

    public List<SubscriptionAO> list(OauthSub sub, String status) {
        if(!sub.isUser()) throw new ErrorBuilder(HttpStatus.FORBIDDEN).exception();
        UUID userId = UUID.fromString(sub.getId());
        List<SubscriptionDO> subscriptions = status != null ?
                repository.findByStatusAndUserId(SubscriptionStatus.fromString(status), userId):
                repository.findByUserId(userId);
        return subscriptions.stream().map((subscription) -> {
            SubscriptionAORsp rsp = toAORsp(subscription);
            rsp.setResults(null);
            rsp.setQuery(null);
            return (SubscriptionAO) rsp;
        }).collect(Collectors.toList());
    }

    //TODO don't implicitly fetch results, use the ocean get method to check for failures.
    public SubscriptionAORsp get(OauthSub sub, String subscriptionId) {
        Optional<SubscriptionDO> found = repository.findBySubscriptionId(UUID.fromString(subscriptionId));
        if(found.isEmpty()) throw new ErrorBuilder(HttpStatus.NOT_FOUND).exception();
        cleanroomService.guard(sub, found.get().getCleanroom().getCleanroomId().toString());
        return toAORsp(found.get());
    }

    public SubscriptionAORsp estimate(OauthSub sub, SubscriptionAOReq req) {
        CleanroomDO cleanroom = cleanroomService.guard(sub, req.getCleanroomId());
        SubscriptionDO subscription = new SubscriptionDO();
        subscription.setSubscriptionId(UUID.randomUUID());
        subscription.setQuery(req.getQuery());
        subscription.setStatus(SubscriptionStatus.ESTIMATE);
        subscription.setName(req.getName().replace("-", "_")); //TODO fix this hacky temp fix.
        subscription.setCleanroom(cleanroom);
        ZonedDateTime now = ZonedDateTime.now();
        subscription.setCreated(now);
        subscription.setModified(now);
        SubscriptionDO saved = repository.save(subscription);
        OceanDO res1 = oceanService.query(saved, OceanType.COUNT, count(req.getQuery()));
        OceanDO res2 = oceanService.query(saved, OceanType.SAMPLE, sample(req.getQuery()));
        saved.setResults(List.of(res1, res2));
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
        update.setModified(ZonedDateTime.now());
        SubscriptionDO saved = repository.save(update);
        OceanDO res = oceanService.query(saved, OceanType.CREATE,
                ctas(saved.getQuery(), saved.getCleanroom().getCleanroomId().toString(), saved.getName()));
        List<OceanDO> results = new ArrayList<>(saved.getResults());
        results.add(res);
        saved.setResults(results);
        return toAORsp(saved);
    }

    private String count(String query) {
        return "SELECT COUNT(*) as \"total\" FROM (" +
                query +
                ");";
    }

    private String sample(String query) {
        return "SELECT * FROM (" +
                query +
                ") LIMIT 10;";
    }

    private String ctas(String query, String cleanroomId, String table) {
        return "CREATE TABLE cr_" + cleanroomId.replace("-", "_") + "." + table +
                " WITH (" +
                "table_type = 'ICEBERG'," +
                "is_external = false," +
                "format = 'PARQUET'," +
                "location = 's3://" + bucket + "/cleanroom/" + cleanroomId + "/') " +
                "AS (" + query + ")";
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
        rsp.setResults(src.getResults() != null ?
                src.getResults().stream().map(oceanService::toAO).collect(Collectors.toList()) : null );
        return rsp;
    }
}
