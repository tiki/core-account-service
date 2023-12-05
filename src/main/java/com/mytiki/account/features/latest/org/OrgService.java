/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.org;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import com.mytiki.account.utilities.facade.StripeF;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@XRayEnabled
public class OrgService {
    private final OrgRepository repository;
    private final StripeF stripe;

    public OrgService(OrgRepository repository, StripeF stripe) {
        this.repository = repository;
        this.stripe = stripe;
    }

    public OrgDO create(String billingEmail){
        try {
            OrgDO org = new OrgDO();
            UUID orgId = UUID.randomUUID();
            String billingId = stripe.create(orgId, billingEmail);
            ZonedDateTime now = ZonedDateTime.now();
            org.setOrgId(orgId);
            org.setBillingId(billingId);
            org.setCreated(now);
            org.setModified(now);
            return repository.save(org);
        }catch (StripeException e) {
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("Failed to create billing profile for org")
                    .exception();
        }
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
            rsp.setProviders(src.getProviders()
                    .stream()
                    .map(provider -> provider.getProviderId().toString())
                    .collect(Collectors.toSet()));
        }

        if(src.getCleanrooms() != null) {
            rsp.setCleanrooms(src.getCleanrooms()
                    .stream()
                    .map(cleanroom -> cleanroom.getCleanroomId().toString())
                    .collect(Collectors.toSet()));
        }

        rsp.setModified(src.getModified());
        rsp.setCreated(src.getCreated());
        return rsp;
    }
}
