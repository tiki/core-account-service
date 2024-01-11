/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.profile;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.api_key.ApiKeyService;
import com.mytiki.account.features.latest.confirm.ConfirmAO;
import com.mytiki.account.features.latest.confirm.ConfirmAction;
import com.mytiki.account.features.latest.confirm.ConfirmService;
import com.mytiki.account.features.latest.org.OrgDO;
import com.mytiki.account.features.latest.org.OrgService;
import com.mytiki.account.features.latest.oauth.OauthScopes;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import com.nimbusds.jose.JOSEException;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@XRayEnabled
public class ProfileService {
    private final ProfileRepository repository;
    private final OrgService orgService;
    private final ConfirmService confirmService;
    private final ApiKeyService apiKeyService;
    private final OauthScopes allowedScopes;

    public ProfileService(
            ProfileRepository repository,
            OrgService orgService,
            ConfirmService confirmService,
            ApiKeyService apiKeyService,
            OauthScopes allowedScopes) {
        this.repository = repository;
        this.orgService = orgService;
        this.confirmService = confirmService;
        this.apiKeyService = apiKeyService;
        this.allowedScopes = allowedScopes;
    }

    public ProfileAO get(String userId){
        Optional<ProfileDO> found = repository.findByUserId(UUID.fromString(userId));
        return found.map(this::toAO).orElseGet(() -> {
            ProfileAO rsp = new ProfileAO();
            rsp.setUserId(userId);
            return rsp;
        });
    }

    public Optional<ProfileDO> getDO(String userId){
        return repository.findByUserId(UUID.fromString(userId));
    }

    public ProfileDO addToOrg(String userId, String orgId, String emailToAdd){
        Optional<ProfileDO> user = getDO(userId);
        if(user.isEmpty() || !user.get().getOrg().getOrgId().toString().equals(orgId))
            throw new ErrorBuilder(HttpStatus.FORBIDDEN).exception();

        Optional<ProfileDO> found = repository.findByEmail(emailToAdd);
        if(found.isEmpty()) {
            throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                    .message("User does not exist")
                    .properties("email", emailToAdd)
                    .exception();
        }else {
            ProfileDO updated = found.get();
            updated.setOrg(user.get().getOrg());
            updated.setModified(ZonedDateTime.now());
            return repository.save(updated);
        }
    }

    public ProfileDO createIfNotExists(String email) {
        Optional<ProfileDO> found = repository.findByEmail(email);
        ProfileDO profile;
        if (found.isEmpty()) {
            OrgDO org = orgService.create(email);
            ProfileDO newUser = new ProfileDO();
            newUser.setUserId(UUID.randomUUID());
            newUser.setEmail(email);
            newUser.setOrg(org);
            ZonedDateTime now = ZonedDateTime.now();
            newUser.setCreated(now);
            newUser.setModified(now);
            profile = repository.save(newUser);
            try {
                OauthScopes scopes = allowedScopes.filter("account:admin trail publish");
                apiKeyService.create(profile, "default", scopes);
            } catch (JOSEException e) {
                throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED).cause(e).exception();
            }
        } else
            profile = found.get();
        return profile;
    }

    public void update(String subject, ProfileAOUpdate update){
        if(update.getEmail() != null) {
            if (!EmailValidator.getInstance().isValid(update.getEmail()))
                throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                        .message("Invalid email")
                        .exception();
        }
        Optional<ProfileDO> found = repository.findByUserId(UUID.fromString(subject));
        if(found.isEmpty())
            throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                    .message("Invalid sub claim")
                    .exception();

        ConfirmAO req = new ConfirmAO();
        req.setEmail(found.get().getEmail());
        req.setAction(ConfirmAction.UPDATE_USER);
        req.setTemplate("user-update");
        req.setOutputs(new HashMap<>(){{
            put("subject", found.get().getEmail());
            put("email", update.getEmail());
        }});
        if(!confirmService.send(req))
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("Failed to send confirmation email.")
                    .properties("email", found.get().getEmail())
                    .exception();
    }

    public void delete(String subject) {
        Optional<ProfileDO> found = repository.findByUserId(UUID.fromString(subject));
        if(found.isEmpty())
            throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                    .message("Invalid sub claim")
                    .exception();
        ConfirmAO req = new ConfirmAO();
        req.setEmail(found.get().getEmail());
        req.setAction(ConfirmAction.DELETE_USER);
        req.setTemplate("user-delete");
        req.setOutputs(new HashMap<>(){{
            put("id", found.get().getId().toString());
        }});
        if(!confirmService.send(req))
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("Failed to send confirmation email.")
                    .properties("email", found.get().getEmail())
                    .exception();
    }

    private ProfileAO toAO(ProfileDO src){
        ProfileAO rsp = new ProfileAO();
        rsp.setUserId(src.getUserId().toString());
        rsp.setEmail(src.getEmail());
        rsp.setCreated(src.getCreated());
        rsp.setModified(src.getModified());
        rsp.setOrgId(src.getOrg().getOrgId().toString());
        return rsp;
    }
}
