/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.confirm;

import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileRepository;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import com.mytiki.account.utilities.facade.RandF;
import com.mytiki.account.utilities.facade.SendgridF;
import com.mytiki.account.utilities.facade.TemplateF;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConfirmService {
    private static final String TEMPLATE_TOKEN = "CONFIRMATION_TOKEN";
    private static final long EXPIRY_MINUTES = 30;

    private final SendgridF sendgrid;
    private final TemplateF template;
    private final ConfirmRepository repository;
    private final ProfileRepository profileRepository;

    public ConfirmService(
            SendgridF sendgrid,
            TemplateF template,
            ConfirmRepository repository,
            ProfileRepository profileRepository) {
        this.sendgrid = sendgrid;
        this.template = template;
        this.repository = repository;
        this.profileRepository = profileRepository;
    }

    public boolean send(ConfirmAO req) {
        String token = RandF.create(16);
        Map<String,String> inputs = new HashMap<>(req.getInputs());
        inputs.put(TEMPLATE_TOKEN, token);
        ConfirmDO confirm = new ConfirmDO();
        confirm.setAction(req.getAction());
        confirm.setProperties(req.getOutputs());
        confirm.setToken(token);
        confirm.setCreated(ZonedDateTime.now());
        repository.save(confirm);
        return sendgrid.send(
                req.getEmail(),
                template.subject(req.getTemplate(), inputs),
                template.html(req.getTemplate(), inputs),
                template.text(req.getTemplate(), inputs));
    }

    public void confirm(String token) {
        Optional<ConfirmDO> found = repository.findByToken(token);
        if(found.isPresent() && found.get().getCreated().plusMinutes(EXPIRY_MINUTES).isAfter(ZonedDateTime.now())){
            process(found.get().getAction(), found.get().getProperties());
            repository.deleteById(found.get().getId());
        }else
            throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                    .message("Invalid or Expired Token")
                    .properties("token", token)
                    .exception();
    }

    private void process(ConfirmAction action, Map<String, String> properties) {
        switch (action){
            case DELETE_USER -> {
                Long id = Long.parseLong(properties.get("id"));
                profileRepository.deleteById(id);
            }
            case UPDATE_USER -> {
                String subject = properties.get("subject");
                String email = properties.get("email");
                Optional<ProfileDO> found = profileRepository.findByEmail(subject);
                if(found.isPresent()){
                    ProfileDO update = found.get();
                    update.setEmail(email);
                    update.setModified(ZonedDateTime.now());
                    profileRepository.save(update);
                }
            }
        }
    }
}
