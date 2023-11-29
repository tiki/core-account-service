/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.cleanroom;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@XRayEnabled
public class CleanroomService {

    private final CleanroomRepository repository;
    private final ProfileService profileService;

    public CleanroomService(
            CleanroomRepository repository,
            ProfileService profileService) {
        this.repository = repository;
        this.profileService = profileService;
    }

    public CleanroomAO create(CleanroomAOReq req, String userId) {
        Optional<ProfileDO> user =  profileService.getDO(userId);
        if(user.isEmpty())
            throw new ErrorBuilder(HttpStatus.FORBIDDEN).exception();
        else {
            //TODO call glue to create.
            //TODO set permissions in s3/glue.
            ZonedDateTime now = ZonedDateTime.now();
            CleanroomDO cleanroom = new CleanroomDO();
            cleanroom.setName(req.getName());
            cleanroom.setCleanroomId(UUID.randomUUID());
            cleanroom.setOrg(user.get().getOrg());
            cleanroom.setAwsAccounts(req.getIam());
            cleanroom.setCreated(now);
            cleanroom.setModified(now);
            return toAO(repository.save(cleanroom));
        }
    }

    public CleanroomAO get(OauthSub sub, String cleanroomId) {
        CleanroomDO cleanroom = guard(sub, cleanroomId);
        return toAO(cleanroom);
    }

    public CleanroomAO update(OauthSub sub, String cleanroomId, CleanroomAOReq req){
        //TODO set permissions in s3/glue.
        CleanroomDO update = guard(sub, cleanroomId);
        if(req.getName() != null) update.setName(req.getName());
        if(req.getIam() != null) update.setAwsAccounts(req.getIam());
        update.setModified(ZonedDateTime.now());
        update = repository.save(update);
        return toAO(update);
    }

    public void delete(OauthSub sub, String cleanroomId){
        CleanroomDO cleanroom = guard(sub, cleanroomId);
        repository.delete(cleanroom);
        //TODO call glue to delete.
    }

    public Optional<CleanroomDO> getDO(String cleanroomId){
        return repository.findByCleanroomId(UUID.fromString(cleanroomId));
    }

    public Optional<CleanroomDO> getDO(String cleanroomId, String userId){
        return repository.findByCleanroomIdAndUserId(UUID.fromString(cleanroomId), UUID.fromString(userId));
    }

    public CleanroomDO guard(OauthSub sub, String cleanroomId){
        if (sub.isInternal()) return getDO(cleanroomId).orElse(null);
        if (sub.isUser()){
            return getDO(cleanroomId, sub.getId()).orElseThrow(() -> new ErrorBuilder(HttpStatus.FORBIDDEN)
                    .detail("Invalid claim: sub")
                    .help("User ID must belong to Cleanroom's Org")
                    .exception());
        }else
            throw new ErrorBuilder(HttpStatus.FORBIDDEN)
                    .detail("Invalid claim: sub")
                    .help("User token required")
                    .exception();
    }

    private CleanroomAO toAO(CleanroomDO src){
        CleanroomAO rsp = new CleanroomAO();
        rsp.setCleanroomId(src.getCleanroomId().toString());
        rsp.setName(src.getName());
        rsp.setModified(src.getModified());
        rsp.setCreated(src.getCreated());
        rsp.setOrgId(src.getOrg().getOrgId().toString());
        rsp.setIam(src.getAwsAccounts());
        return rsp;
    }
}
