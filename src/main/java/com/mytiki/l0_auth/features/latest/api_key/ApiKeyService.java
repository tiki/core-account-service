/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.api_key;

import com.mytiki.l0_auth.features.latest.app_info.AppInfoDO;
import com.mytiki.l0_auth.features.latest.app_info.AppInfoService;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoAO;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoService;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ApiKeyService {
    private final ApiKeyRepository repository;
    private final UserInfoService userInfoService;
    private final AppInfoService appInfoService;

    public ApiKeyService(
            ApiKeyRepository repository,
            UserInfoService userInfoService,
            AppInfoService appInfoService) {
        this.repository = repository;
        this.userInfoService = userInfoService;
        this.appInfoService = appInfoService;
    }

    public ApiKeyAO create(String userId, String appId){
        guardAppUser(userId, appId);
        Optional<AppInfoDO> app = appInfoService.getDO(appId);
        if(app.isEmpty())
            throw new ApiExceptionBuilder(HttpStatus.BAD_REQUEST)
                    .message("Invalid App")
                    .help("Get valid appIds from ../oauth/userinfo")
                    .build();

        ApiKeyDO apiKey = new ApiKeyDO();
        apiKey.setId(UUID.randomUUID());
        apiKey.setApp(app.get());
        apiKey.setCreated(ZonedDateTime.now());
        repository.save(apiKey);

        ApiKeyAO rsp = new ApiKeyAO();
        rsp.setId(apiKey.toString());
        rsp.setCreated(apiKey.getCreated());
        //rsp.setSecret();
        return rsp;
    }

    public List<ApiKeyAO> getByAppId(String userId, String appId){
        guardAppUser(userId, appId);
        List<ApiKeyDO> keys = repository.findAllByAppAppId(UUID.fromString(appId));
        return keys.stream().map(key -> {
            ApiKeyAO rsp = new ApiKeyAO();
            rsp.setId(key.getId().toString());
            rsp.setCreated(key.getCreated());
            return rsp;
        }).toList();
    }

    private void guardAppUser(String userId, String appId){
        UserInfoAO user = userInfoService.get(userId);
        if(!user.getApps().contains(appId))
            throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN).build();
    }
}
