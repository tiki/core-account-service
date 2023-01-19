/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.app_info;

import com.mytiki.l0_auth.features.latest.user_info.UserInfoDO;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

public class AppInfoService {

    private final AppInfoRepository repository;

    public AppInfoService(AppInfoRepository repository) {
        this.repository = repository;
    }

    public AppInfoDO create(String name, UserInfoDO user){
        ZonedDateTime now = ZonedDateTime.now();
        AppInfoDO app = new AppInfoDO();
        app.setName(name);
        app.setUsers(Set.of(user));
        app.setAppId(UUID.randomUUID());
        app.setCreated(now);
        app.setModified(now);
        return repository.save(app);
    }
}
