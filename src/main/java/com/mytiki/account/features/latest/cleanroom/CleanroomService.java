/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.cleanroom;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.event.EventDO;
import com.mytiki.account.features.latest.event.EventService;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@XRayEnabled
public class CleanroomService {

    private final CleanroomRepository repository;
    private final ProfileService profileService;
    private final EventService eventService;

    public CleanroomService(
            CleanroomRepository repository,
            ProfileService profileService,
            EventService eventService) {
        this.repository = repository;
        this.profileService = profileService;
        this.eventService = eventService;
    }

    public CleanroomAORsp create(CleanroomAOReq req, OauthSub sub) {
        if(!sub.isUser()) throw new ErrorBuilder(HttpStatus.FORBIDDEN).message("User token required").exception();
        Optional<ProfileDO> user =  profileService.getDO(sub.getId());
        if(user.isEmpty())
            throw new ErrorBuilder(HttpStatus.FORBIDDEN).exception();
        else {
            ZonedDateTime now = ZonedDateTime.now();
            CleanroomDO cleanroom = new CleanroomDO();
            UUID id = UUID.randomUUID();
            cleanroom.setCleanroomId(id);
            cleanroom.setName("cr_" + id.toString().replace('-', '_'));
            cleanroom.setOrg(user.get().getOrg());
            cleanroom.setCreated(now);
            cleanroom.setModified(now);
            cleanroom.setAws(req.getAws());
            cleanroom.setDescription(req.getDescription());
            EventDO event = eventService.createCleanroom(cleanroom);
            cleanroom.setEvents(List.of(event));
            cleanroom = repository.save(cleanroom);
            return toAORsp(cleanroom);
        }
    }

    public CleanroomAORsp get(OauthSub sub, String cleanroomId) {
        CleanroomDO cleanroom = guard(sub, cleanroomId);
        return toAORsp(cleanroom);
    }

    public List<CleanroomAO> list(OauthSub sub) {
        if (sub.isUser()){
            Optional<ProfileDO> profile = profileService.getDO(sub.getId());
            if(profile.isEmpty()) throw new ErrorBuilder(HttpStatus.NOT_FOUND)
                    .message("Missing User ID")
                    .help("Check authorization token")
                    .exception();
            else {
                List<CleanroomDO> cleanrooms = profile.get().getOrg().getCleanrooms();
                return cleanrooms.stream()
                        .map(cleanroom -> {
                            CleanroomAO rsp = new CleanroomAO();
                            rsp.setCleanroomId(cleanroom.getCleanroomId().toString());
                            rsp.setName(cleanroom.getName());
                            rsp.setDescription(cleanroom.getDescription());
                            return rsp;
                        }).toList();
            }
        }else
            throw new ErrorBuilder(HttpStatus.FORBIDDEN)
                    .detail("Invalid claim: sub")
                    .help("User token required")
                    .exception();
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

    private CleanroomAORsp toAORsp(CleanroomDO src){
        CleanroomAORsp rsp = new CleanroomAORsp();
        rsp.setCleanroomId(src.getCleanroomId().toString());
        rsp.setName(src.getName());
        rsp.setAws(src.getAws());
        rsp.setDescription(src.getDescription());
        rsp.setModified(src.getModified());
        rsp.setCreated(src.getCreated());
        rsp.setOrgId(src.getOrg().getOrgId().toString());
        rsp.setEvents(src.getEvents().stream().map(eventService::toAORsp).collect(Collectors.toList()));
        return rsp;
    }
}
