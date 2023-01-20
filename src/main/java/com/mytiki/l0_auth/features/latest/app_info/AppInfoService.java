/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.app_info;

import com.mytiki.l0_auth.features.latest.user_info.UserInfoDO;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AppInfoService {

    private final AppInfoRepository repository;

    public AppInfoService(AppInfoRepository repository) {
        this.repository = repository;
    }

    public AppInfoAO create(String name, UserInfoDO user){
        ZonedDateTime now = ZonedDateTime.now();
        AppInfoDO app = new AppInfoDO();
        app.setName(name);
        app.setUsers(Set.of(user));
        app.setAppId(UUID.randomUUID());
        app.setCreated(now);
        app.setModified(now);
        return toAO(repository.save(app));
    }

    public AppInfoAO get(String appId){
        Optional<AppInfoDO> found = repository.findByAppId(UUID.fromString(appId));
        return found.map(this::toAO).orElseGet(() -> {
                AppInfoAO rsp = new AppInfoAO();
                rsp.setAppId(appId);
                return rsp;
        });
    }

    public Optional<AppInfoDO> getDO(String appId){
        return repository.findByAppId(UUID.fromString(appId));
    }

    private AppInfoAO toAO(AppInfoDO src){
        AppInfoAO rsp = new AppInfoAO();
        rsp.setAppId(src.getAppId().toString());
        rsp.setName(src.getName());
        rsp.setModified(src.getModified());
        rsp.setCreated(src.getCreated());
        if(src.getUsers() != null)
            rsp.setUsers(src.getUsers().stream().map(u -> u.getUserId().toString()).collect(Collectors.toSet()));
        return rsp;
    }
}
