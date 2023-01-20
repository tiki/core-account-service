/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.user_info;

import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserInfoService {

    private final UserInfoRepository repository;

    public UserInfoService(UserInfoRepository repository) {
        this.repository = repository;
    }

    public UserInfoAO get(String userId){
        Optional<UserInfoDO> found = repository.findByUserId(UUID.fromString(userId));
        return found.map(this::toAO).orElseGet(() -> {
            UserInfoAO rsp = new UserInfoAO();
            rsp.setSub(userId);
            rsp.setUserId(userId);
            return rsp;
        });
    }

    public UserInfoAO createIfNotExists(String email) {
        Optional<UserInfoDO> found = repository.findByEmail(email);
        UserInfoDO userInfo;
        if (found.isEmpty()) {
            UserInfoDO newUser = new UserInfoDO();
            newUser.setUserId(UUID.randomUUID());
            newUser.setEmail(email);
            ZonedDateTime now = ZonedDateTime.now();
            newUser.setCreated(now);
            newUser.setModified(now);
            userInfo = repository.save(newUser);
        } else
            userInfo = found.get();
        return toAO(userInfo);
    }

    public UserInfoAO update(String subject, UserInfoAOUpdate update){
        Optional<UserInfoDO> found = repository.findByUserId(UUID.fromString(subject));
        if(found.isEmpty())
            throw new ApiExceptionBuilder(HttpStatus.BAD_REQUEST)
                    .message("Invalid sub claim")
                    .build();

        UserInfoDO saved = found.get();
        if(update.getEmail() != null)
            saved.setEmail(update.getEmail().toLowerCase());
        saved.setModified(ZonedDateTime.now());
        saved = repository.save(saved);
        return toAO(saved);
    }

    private UserInfoAO toAO(UserInfoDO src){
        UserInfoAO rsp = new UserInfoAO();
        rsp.setSub(src.getUserId().toString());
        rsp.setUserId(src.getUserId().toString());
        rsp.setEmail(src.getEmail());
        rsp.setCreated(src.getCreated());
        rsp.setModified(src.getModified());
        if(src.getApps() != null)
            rsp.setApps(src.getApps().stream().map(a -> a.getAppId().toString()).collect(Collectors.toSet()));
        return rsp;
    }
}
