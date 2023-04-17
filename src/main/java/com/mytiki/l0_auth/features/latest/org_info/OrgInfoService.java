/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.org_info;

import com.mytiki.l0_auth.features.latest.user_info.UserInfoDO;
import com.mytiki.l0_auth.features.latest.user_info.UserInfoService;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrgInfoService {

    private final OrgInfoRepository repository;
    private final UserInfoService userInfoService;

    public OrgInfoService(OrgInfoRepository repository, UserInfoService userInfoService) {
        this.repository = repository;
        this.userInfoService = userInfoService;
    }

    public OrgInfoDO create(){
        OrgInfoDO org = new OrgInfoDO();
        ZonedDateTime now = ZonedDateTime.now();
        org.setOrgId(UUID.randomUUID());
        org.setCreated(now);
        org.setModified(now);
        return repository.save(org);
    }

    public void addUser(String userId, String orgId, OrgInfoAOReq req){
        Optional<UserInfoDO> user = userInfoService.getDO(userId);
        if(user.isEmpty() || !user.get().getOrg().getOrgId().toString().equals(orgId))
            throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN).build();
        userInfoService.addToOrg(req.getEmail(), user.get().getOrg());
    }

    @Transactional
    public OrgInfoAO get(String userId, String orgId){
        Optional<OrgInfoDO> found = repository.findByOrgId(UUID.fromString(orgId));
        if(found.isPresent()){
            List<String> allowedUserIds = found.get().getUsers()
                    .stream()
                    .map(user -> user.getUserId().toString())
                    .toList();
            if(!allowedUserIds.contains(userId))
                throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN).build();
            return toAO(found.get());
        }else{
            OrgInfoAO rsp = new OrgInfoAO();
            rsp.setOrgId(orgId);
            return rsp;
        }
    }

//    public OrgInfoDO setBilling(String orgId, String billingId){
//
//    }

    private OrgInfoAO toAO(OrgInfoDO src){
        OrgInfoAO rsp = new OrgInfoAO();
        rsp.setOrgId(src.getOrgId().toString());
        rsp.setBillingId(src.getBillingId());

        if(src.getUsers() != null) {
            rsp.setUsers(src.getUsers()
                    .stream()
                    .map(user -> user.getUserId().toString())
                    .collect(Collectors.toSet()));
        }

        if(src.getApps() != null) {
            rsp.setApps(src.getApps()
                    .stream()
                    .map(app -> app.getAppId().toString())
                    .collect(Collectors.toSet()));
        }

        rsp.setModified(src.getModified());
        rsp.setCreated(src.getCreated());
        return rsp;
    }
}
