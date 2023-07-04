/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.app_info;

import com.mytiki.account.features.latest.user_info.UserInfoDO;
import com.mytiki.account.features.latest.user_info.UserInfoService;
import com.mytiki.account.security.oauth.OauthSub;
import com.mytiki.account.utilities.facade.B64F;
import com.mytiki.account.utilities.facade.RsaF;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import com.nimbusds.jose.JOSEException;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
           try {
               app.setSignKey(RsaF.generate());
           } catch (JOSEException e) {
               throw new ApiExceptionBuilder(HttpStatus.EXPECTATION_FAILED)
                       .message("Issue with Sign Key")
                       .detail(e.getMessage())
                       .help("Please contact support")
                       .build();
           }
           return toAO(repository.save(app));
       }
    }

    @Transactional
    public AppInfoAO getForUser(String userId, String appId){
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

    public AppInfoAO get(String appId){
        Optional<AppInfoDO> found = repository.findByAppId(UUID.fromString(appId));
        return found.map(this::toAO).orElse(null);
    }

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
        try{
            RSAPublicKey pubkey = RsaF.toPublic(src.getSignKey());
            rsp.setPubKey(B64F.encode(pubkey.getEncoded()));
        } catch (IOException e) {
            throw new ApiExceptionBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("Issue with Sign Key")
                    .detail(e.getMessage())
                    .help("Please contact support")
                    .build();
        }
        return rsp;
    }

    public void guard(JwtAuthenticationToken token, String appId){
        boolean isInternal = token.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals("SCOPE_account:internal:read"));
        if(isInternal) return;
        OauthSub sub = new OauthSub(token.getName());
        if (sub.isApp() && !sub.getId().equals(appId)) {
            throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN)
                    .detail("Invalid claim: sub")
                    .help("App ID does not match claim")
                    .build();
        }else if (sub.isUser()){
            Optional<UserInfoDO> user = userInfoService.getDO(sub.getId());
            if (user.isEmpty())
                throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN)
                        .detail("Invalid claim: sub")
                        .help("Invalid User ID")
                        .build();
            Optional<AppInfoDO> app = repository.findByAppId(UUID.fromString(appId));
            if (app.isPresent()) {
                if (!app.get().getOrg().getUsers().contains(user.get()))
                    throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN)
                            .detail("Invalid claim: sub")
                            .help("User ID must belong to App's Org")
                            .build();
            }
        }
    }
}
