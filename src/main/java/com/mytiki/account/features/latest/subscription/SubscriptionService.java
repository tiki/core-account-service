/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import com.mytiki.account.features.latest.cleanroom.CleanroomAOReq;
import com.mytiki.account.features.latest.cleanroom.CleanroomDO;
import com.mytiki.account.features.latest.cleanroom.CleanroomService;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.ocean.OceanDO;
import com.mytiki.account.features.latest.ocean.OceanService;
import com.mytiki.account.features.latest.ocean.OceanType;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    public SubscriptionAO get(OauthSub sub, String subscriptionId) {
        Optional<SubscriptionDO> found = repository.findBySubscriptionId(UUID.fromString(subscriptionId));
        if(found.isEmpty()) throw new ErrorBuilder(HttpStatus.NOT_FOUND).exception();
        cleanroomService.guard(sub, found.get().getCleanroom().getCleanroomId().toString());
        return toAO(found.get());
    }

    public SubscriptionAO estimate(OauthSub sub, SubscriptionAOReq req) {
        CleanroomDO cleanroom = cleanroomService.guard(sub, req.getCleanroomId());
        SubscriptionDO subscription = new SubscriptionDO();
        subscription.setSubscriptionId(UUID.randomUUID());
        subscription.setQuery(req.getQuery());
        subscription.setStatus(SubscriptionStatus.ESTIMATE);
        subscription.setCleanroom(cleanroom);
        ZonedDateTime now = ZonedDateTime.now();
        subscription.setCreated(now);
        subscription.setModified(now);
        SubscriptionDO saved = repository.save(subscription);
        OceanDO res1 = oceanService.query(saved, OceanType.COUNT, count(req.getQuery()));
        OceanDO res2 = oceanService.query(saved, OceanType.SAMPLE, sample(req.getQuery()));
        saved.setResults(List.of(res1, res2));
        return toAO(saved);
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

    private SubscriptionAO toAO(SubscriptionDO src) {
        SubscriptionAO rsp = new SubscriptionAO();
        rsp.setCreated(src.getCreated());
        rsp.setModified(src.getModified());
        rsp.setSubscriptionId(src.getSubscriptionId().toString());
        rsp.setCleanroomId(src.getCleanroom().getCleanroomId().toString());
        rsp.setQuery(src.getQuery());
        rsp.setStatus(src.getStatus().toString());
        rsp.setResults(src.getResults() != null ?
                src.getResults().stream().map(oceanService::toAO).collect(Collectors.toList()) : null );
        return rsp;
    }
}
