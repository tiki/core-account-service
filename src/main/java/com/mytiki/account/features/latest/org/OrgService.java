/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.org;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@XRayEnabled
public class OrgService {
    private final OrgRepository repository;

    public OrgService(OrgRepository repository) {
        this.repository = repository;
    }

    public OrgDO create(){
        OrgDO org = new OrgDO();
        ZonedDateTime now = ZonedDateTime.now();
        org.setOrgId(UUID.randomUUID());
        org.setCreated(now);
        org.setModified(now);
        return repository.save(org);
    }

    @Transactional
    public OrgAO getByUser(String userId){
        Optional<OrgDO> found = repository.findByUserId(UUID.fromString(userId));
        return found.map(this::toAO).orElse(null);
    }

    @Transactional
    public OrgAO get(String orgId){
        Optional<OrgDO> found = repository.findByOrgId(UUID.fromString(orgId));
        return found.map(this::toAO).orElse(null);
    }

    private OrgAO toAO(OrgDO src){
        OrgAO rsp = new OrgAO();
        rsp.setOrgId(src.getOrgId().toString());
        rsp.setBillingId(src.getBillingId());

        if(src.getProfiles() != null) {
            rsp.setUsers(src.getProfiles()
                    .stream()
                    .map(user -> user.getUserId().toString())
                    .collect(Collectors.toSet()));
        }

        if(src.getProviders() != null) {
            rsp.setApps(src.getProviders()
                    .stream()
                    .map(app -> app.getProviderId().toString())
                    .collect(Collectors.toSet()));
        }

        rsp.setModified(src.getModified());
        rsp.setCreated(src.getCreated());
        return rsp;
    }
}
