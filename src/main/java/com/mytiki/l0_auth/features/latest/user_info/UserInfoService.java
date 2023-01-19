/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.user_info;

import com.mytiki.spring_rest_api.ApiException;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class UserInfoService {

    private final UserInfoRepository repository;
    private final JwtDecoder jwtDecoder;

    public UserInfoService(
            UserInfoRepository repository,
            JwtDecoder jwtDecoder) {
        this.repository = repository;
        this.jwtDecoder = jwtDecoder;
    }

    public UserInfoAO get(String token){
        Jwt jwt = jwtDecoder.decode(token);
        if(jwt.getSubject() == null)
            throw new ApiException(HttpStatus.FORBIDDEN);

        Optional<UserInfoDO> found = repository.findByUserId(UUID.fromString(jwt.getSubject()));
        UserInfoAO rsp = new UserInfoAO();
        if(found.isPresent()){
            rsp.setSub(found.get().getUserId().toString());
            rsp.setEmail(found.get().getEmail());
            rsp.setUpdatedAt(found.get().getModified());
        }
        return rsp;
    }

    public UserInfoDO createIfNotExists(String email){
        Optional<UserInfoDO> found = repository.findByEmail(email);
        if(found.isPresent())
            return found.get();
        else {
            UserInfoDO userInfo = new UserInfoDO();
            userInfo.setUserId(UUID.randomUUID());
            userInfo.setEmail(email);
            ZonedDateTime now = ZonedDateTime.now();
            userInfo.setCreated(now);
            userInfo.setModified(now);
            return repository.save(userInfo);
        }
    }

    public UserInfoAO update(String token, UserInfoAOUpdate update){
        Jwt jwt = jwtDecoder.decode(token);
        Optional<UserInfoDO> found = repository.findByUserId(UUID.fromString(jwt.getSubject()));
        if(found.isEmpty())
            throw new ApiExceptionBuilder(HttpStatus.BAD_REQUEST)
                    .message("Invalid sub claim")
                    .build();

        UserInfoDO saved = found.get();
        if(update.getEmail() != null)
            saved.setEmail(update.getEmail().toLowerCase());
        saved.setModified(ZonedDateTime.now());
        saved = repository.save(saved);

        UserInfoAO rsp = new UserInfoAO();
        rsp.setSub(saved.getUserId().toString());
        rsp.setEmail(saved.getEmail());
        rsp.setUpdatedAt(saved.getModified());
        return rsp;
    }
}
