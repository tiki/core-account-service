/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.app_info;

import com.mytiki.l0_auth.features.latest.user_info.UserInfoDO;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoService;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AppInfoService {

    private final AppInfoRepository repository;
    private final UserInfoService userInfoService;

    public AppInfoService(AppInfoRepository repository, UserInfoService userInfoService) {
        this.repository = repository;
        this.userInfoService = userInfoService;
    }

    @Transactional
    public AppInfoAO create(String name, String userId){
       Optional<UserInfoDO> user =  userInfoService.getDO(userId);
       if(user.isEmpty())
           throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN).build();
       else {
           ZonedDateTime now = ZonedDateTime.now();
           AppInfoDO app = new AppInfoDO();
           app.setName(name);
           app.setOrg(user.get().getOrg());
           app.setAppId(UUID.randomUUID());
           app.setCreated(now);
           app.setModified(now);
           return toAO(repository.save(app));
       }
    }

    @Transactional
    public AppInfoAO get(String userId, String appId){
        Optional<AppInfoDO> found = repository.findByAppId(UUID.fromString(appId));
        if(found.isPresent()){
            List<String> allowedUserIds = found.get().getOrg().getUsers()
                    .stream()
                    .map(user -> user.getUserId().toString())
                    .toList();
            if(!allowedUserIds.contains(userId))
                throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN).build();
            return toAO(found.get());
        }else{
            AppInfoAO rsp = new AppInfoAO();
            rsp.setAppId(appId);
            return rsp;
        }
    }

    @Transactional
    public AppInfoAO update(String userId, String appId, AppInfoAOReq req){
        Optional<UserInfoDO> user =  userInfoService.getDO(userId);
        if(user.isEmpty())
            throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN).build();

        Optional<AppInfoDO> found = repository.findByAppId(UUID.fromString(appId));
        if(found.isEmpty())
            throw new ApiExceptionBuilder(HttpStatus.BAD_REQUEST)
                    .detail("Invalid App ID")
                    .build();

        if(!found.get().getOrg().getUsers().contains(user.get()))
            throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN).build();

        AppInfoDO update = found.get();
        update.setName(req.getName());
        update = repository.save(update);
        return toAO(update);
    }

    @Transactional
    public void delete(String userId, String appId){
        Optional<UserInfoDO> user =  userInfoService.getDO(userId);
        if(user.isEmpty())
            throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN).build();
        Optional<AppInfoDO> app = repository.findByAppId(UUID.fromString(appId));
        if(app.isPresent()) {
            if(!app.get().getOrg().getUsers().contains(user.get()))
                throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN).build();
            repository.delete(app.get());
        }
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
        rsp.setOrgId(src.getOrg().getOrgId().toString());
        return rsp;
    }
}
