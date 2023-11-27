/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import com.mytiki.account.features.latest.ocean.OceanDO;
import com.mytiki.account.features.latest.ocean.OceanService;
import com.mytiki.account.features.latest.ocean.OceanType;
import com.mytiki.account.features.latest.profile.ProfileDO;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionService {
    private final SubscriptionRepository repository;
    private final OceanService ocean;

    public SubscriptionService(SubscriptionRepository repository, OceanService ocean) {
        this.repository = repository;
        this.ocean = ocean;
    }

    public SubscriptionDO estimate(ProfileDO profile, String query) {
        SubscriptionDO subscription = new SubscriptionDO();
        subscription.setQuery(query);
        subscription.setStatus(SubscriptionStatus.ESTIMATE);
        subscription.setProfile(profile);
        ZonedDateTime now = ZonedDateTime.now();
        subscription.setCreated(now);
        subscription.setModified(now);
        SubscriptionDO saved = repository.save(subscription);
        OceanDO res1 = ocean.query(saved, OceanType.COUNT, count(query));
        OceanDO res2 = ocean.query(saved, OceanType.SAMPLE, sample(query));
        saved.setResults(List.of(res1, res2));
        return saved;
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
}
