/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.event;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.event.ao.EventAOCrCreateRsp;
import com.mytiki.account.features.latest.event.ao.EventAOErrorRsp;
import com.mytiki.account.features.latest.event.ao.EventAOSubEstimateRsp;
import com.mytiki.account.features.latest.event.ao.EventAOSubPurchaseRsp;
import com.mytiki.account.features.latest.event.type.EventType;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@XRayEnabled
@RestController
@RequestMapping(value = EventCallback.ROUTE)
public class EventCallback {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "event";

    private final EventHandler handler;

    public EventCallback(EventHandler handler){
        this.handler = handler;
    }

    @Operation(hidden = true)
    @Secured("SCOPE_account:internal:event")
    @RequestMapping(method = RequestMethod.POST, path = "/error")
    @ResponseStatus(code = HttpStatus.OK)
    public void error(@RequestBody EventAOErrorRsp body) {
        handler.error(body);
    }

    @Operation(hidden = true)
    @Secured("SCOPE_account:internal:event")
    @RequestMapping(method = RequestMethod.POST, path = "/cleanroom/create")
    @ResponseStatus(code = HttpStatus.OK)
    public void crCreate(@RequestBody EventAOCrCreateRsp body) {
        handler.process(EventType.CREATE_CLEANROOM, body);
    }

    @Operation(hidden = true)
    @Secured("SCOPE_account:internal:event")
    @RequestMapping(method = RequestMethod.POST, path = "/subscription/estimate")
    @ResponseStatus(code = HttpStatus.OK)
    public void subEstimate(@RequestBody EventAOSubEstimateRsp body) {
        handler.process(EventType.ESTIMATE_SUBSCRIPTION, body);
    }

    @Operation(hidden = true)
    @Secured("SCOPE_account:internal:event")
    @RequestMapping(method = RequestMethod.POST, path = "/subscription/purchase")
    @ResponseStatus(code = HttpStatus.OK)
    public void subPurchase(@RequestBody EventAOSubPurchaseRsp body) {
        handler.process(EventType.PURCHASE_SUBSCRIPTION, body);
    }
}
