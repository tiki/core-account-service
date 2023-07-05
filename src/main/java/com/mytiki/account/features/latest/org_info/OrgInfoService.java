/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.org_info;

import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrgInfoService {
    private final OrgInfoRepository repository;

    public OrgInfoService(OrgInfoRepository repository) {
        this.repository = repository;
    }

    public OrgInfoDO create(){
        OrgInfoDO org = new OrgInfoDO();
        ZonedDateTime now = ZonedDateTime.now();
        org.setOrgId(UUID.randomUUID());
        org.setCreated(now);
        org.setModified(now);
        return repository.save(org);
    }

    public OrgInfoAO getByUser(String userId){
        Optional<OrgInfoDO> found = repository.findByUserId(UUID.fromString(userId));
        return found.map(this::toAO).orElse(null);
    }

    public OrgInfoAO get(String orgId){
        Optional<OrgInfoDO> found = repository.findByOrgId(UUID.fromString(orgId));
        return found.map(this::toAO).orElse(null);
    }

    public OrgInfoDO setBilling(String orgId, String billingId){
        Optional<OrgInfoDO> found = repository.findByOrgId(UUID.fromString(orgId));
        if(found.isPresent()){
            OrgInfoDO updated = found.get();
            updated.setBillingId(billingId);
            updated.setModified(ZonedDateTime.now());
            return repository.save(updated);
        }else{
            throw new ApiExceptionBuilder(HttpStatus.BAD_REQUEST)
                    .message("Invalid orgId")
                    .properties("orgId", orgId)
                    .build();
        }
    }

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
