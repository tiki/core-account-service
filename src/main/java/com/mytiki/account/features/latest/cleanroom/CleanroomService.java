/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.cleanroom;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.oauth.OauthScopes;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

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

    public CleanroomAO get(String cleanroomId) {
        Optional<CleanroomDO> found = getDO(cleanroomId);
        return found.map(this::toAO).orElse(null);
    }

    public CleanroomAO update(String cleanroomId, CleanroomAOReq req){
        //TODO set permissions in s3/glue.
        Optional<CleanroomDO> found = repository.findByCleanroomId(UUID.fromString(cleanroomId));
        if(found.isEmpty())
            throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                    .detail("Invalid Cleanroom ID")
                    .exception();
        CleanroomDO update = found.get();
        if(req.getName() != null) update.setName(req.getName());
        if(req.getIam() != null) update.setAwsAccounts(req.getIam());
        update.setModified(ZonedDateTime.now());
        update = repository.save(update);
        return toAO(update);
    }

    public void delete(String cleanroomId){
        //TODO call glue to delete.
        repository.deleteByCleanroomId(UUID.fromString(cleanroomId));
    }

    public Optional<CleanroomDO> getDO(String cleanroomId){
        return repository.findByCleanroomId(UUID.fromString(cleanroomId));
    }

    public void guard(JwtAuthenticationToken token, String cleanroomId){
        if(OauthScopes.hasScope(token,"account:internal:read")) return;
        OauthSub sub = new OauthSub(token.getName());
        if (sub.isUser()){
            Optional<CleanroomDO> cleanroom = repository.findByCleanroomIdAndUserId(UUID.fromString(cleanroomId), UUID.fromString(sub.getId()));
            if (cleanroom.isEmpty())
                throw new ErrorBuilder(HttpStatus.FORBIDDEN)
                        .detail("Invalid claim: sub")
                        .help("User ID must belong to Cleanroom's Org")
                        .exception();
        }
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
