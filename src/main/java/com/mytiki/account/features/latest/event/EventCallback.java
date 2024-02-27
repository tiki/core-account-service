/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.event;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.event.ao.*;
import com.mytiki.account.features.latest.event.status.EventStatus;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@XRayEnabled
@RestController
@RequestMapping(value = EventCallback.ROUTE)
public class EventCallback {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "ocean";

    private final EventRepository repository;
    private final ObjectMapper mapper;


    public EventCallback(EventRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Operation(hidden = true)
    @Secured("SCOPE_account:internal:ocean")
    @RequestMapping(method = RequestMethod.POST, path = "/error")
    @ResponseStatus(code = HttpStatus.OK)
    public void error(@RequestBody EventAOErrorRsp body) { handle(EventStatus.FAILED, body); }

    @Operation(hidden = true)
    @Secured("SCOPE_account:internal:ocean")
    @RequestMapping(method = RequestMethod.POST, path = "/cleanroom/create")
    @ResponseStatus(code = HttpStatus.OK)
    public void crCreate(@RequestBody EventAOCrCreateRsp body) { handle(body); }

    @Operation(hidden = true)
    @Secured("SCOPE_account:internal:ocean")
    @RequestMapping(method = RequestMethod.POST, path = "/subscription/estimate")
    @ResponseStatus(code = HttpStatus.OK)
    public void subEstimate(@RequestBody EventAOSubEstimateRsp body) { handle(body); }

    @Operation(hidden = true)
    @Secured("SCOPE_account:internal:ocean")
    @RequestMapping(method = RequestMethod.POST, path = "/estimate/purchase")
    @ResponseStatus(code = HttpStatus.OK)
    public void subPurchase(@RequestBody EventAOSubPurchaseRsp body) {
        EventDO event = handle(body);
        //todo: report usage by calling the subscription service.
    }

    public EventDO handle(EventAOBase rsp) { return handle(EventStatus.SUCCESS, rsp); }

    public EventDO handle(EventStatus status, EventAOBase rsp) {
        UUID requestId = UUID.fromString(rsp.getRequestId());
        Optional<EventDO> found = repository.findByRequestId(requestId);
        if(found.isEmpty()) throw new ErrorBuilder(HttpStatus.NOT_FOUND)
                .message("Invalid requestId")
                .properties("requestId", requestId.toString())
                .exception();
        else {
            EventDO update = found.get();
            try{
                String res = mapper.writeValueAsString(rsp);
                update.setStatus(status);
                update.setResult(res);
                return repository.save(update);
            } catch (JsonProcessingException e) {
                update.setStatus(EventStatus.FAILED);
                repository.save(update);
                throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                        .message(e.getMessage())
                        .properties("requestId", requestId.toString())
                        .cause(e)
                        .exception();
            }
        }
    }
}
