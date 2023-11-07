/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.org_info;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@XRayEnabled
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

    @Transactional
    public OrgInfoAO getByUser(String userId){
        Optional<OrgInfoDO> found = repository.findByUserId(UUID.fromString(userId));
        return found.map(this::toAO).orElse(null);
    }

    @Transactional
    public OrgInfoAO get(String orgId){
        Optional<OrgInfoDO> found = repository.findByOrgId(UUID.fromString(orgId));
        return found.map(this::toAO).orElse(null);
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
