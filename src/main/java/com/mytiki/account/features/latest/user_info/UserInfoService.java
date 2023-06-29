/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.user_info;

import com.mytiki.account.features.latest.org_info.OrgInfoService;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class UserInfoService {

    private final UserInfoRepository repository;
    private final OrgInfoService orgInfoService;

    public UserInfoService(UserInfoRepository repository, OrgInfoService orgInfoService) {
        this.repository = repository;
        this.orgInfoService = orgInfoService;
    }

    public UserInfoAO get(String userId){
        Optional<UserInfoDO> found = repository.findByUserId(UUID.fromString(userId));
        return found.map(this::toAO).orElseGet(() -> {
            UserInfoAO rsp = new UserInfoAO();
            rsp.setUserId(userId);
            return rsp;
        });
    }

    public Optional<UserInfoDO> getDO(String userId){
        return repository.findByUserId(UUID.fromString(userId));
    }

    public UserInfoDO addToOrg(String userId, String orgId, String emailToAdd){
        Optional<UserInfoDO> user = getDO(userId);
        if(user.isEmpty() || !user.get().getOrg().getOrgId().toString().equals(orgId))
            throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN).build();

        Optional<UserInfoDO> found = repository.findByEmail(emailToAdd);
        if(found.isEmpty()) {
            throw new ApiExceptionBuilder(HttpStatus.BAD_REQUEST)
                    .message("User does not exist")
                    .properties("email", emailToAdd)
                    .build();
        }else {
            UserInfoDO updated = found.get();
            updated.setOrg(user.get().getOrg());
            updated.setModified(ZonedDateTime.now());
            return repository.save(updated);
        }
    }

    public UserInfoAO createIfNotExists(String email) {
        Optional<UserInfoDO> found = repository.findByEmail(email);
        UserInfoDO userInfo;
        if (found.isEmpty()) {
            UserInfoDO newUser = new UserInfoDO();
            newUser.setUserId(UUID.randomUUID());
            newUser.setEmail(email);
            newUser.setOrg(orgInfoService.create());
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
        rsp.setUserId(src.getUserId().toString());
        rsp.setEmail(src.getEmail());
        rsp.setCreated(src.getCreated());
        rsp.setModified(src.getModified());
        rsp.setOrgId(src.getOrg().getOrgId().toString());
        return rsp;
    }
}
