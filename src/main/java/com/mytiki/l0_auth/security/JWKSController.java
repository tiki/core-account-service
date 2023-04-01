/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.security;

import com.nimbusds.jose.jwk.JWKSet;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = JWKSController.PATH)
public class JWKSController {
    public static final String PATH = "/.well-known/jwks.json";
    private final JWKSet jwkSet;

    public JWKSController(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
    }

    @Operation(hidden = true)
    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Object> get() {
        return jwkSet.toJSONObject(true);
    }
}
