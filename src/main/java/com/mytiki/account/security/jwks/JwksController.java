/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.security.jwks;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.nimbusds.jose.jwk.JWKSet;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@XRayEnabled
@RestController
@RequestMapping(value = JwksController.ROUTE)
public class JwksController {
    public static final String ROUTE = "/.well-known/jwks.json";
    private final JWKSet jwkSet;

    public JwksController(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
    }

    @Operation(hidden = true)
    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Object> get() {
        return jwkSet.toJSONObject(true);
    }
}
