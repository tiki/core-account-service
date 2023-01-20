/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.security;

import com.mytiki.l0_auth.utilities.Constants;
import com.nimbusds.jose.jwk.JWKSet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "AUTH")
@RestController
@RequestMapping(value = "/.well-known/jwks.json")
public class JWKSController {
    private final JWKSet jwkSet;

    public JWKSController(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-jwks-get",
            summary = "JWKS endpoint", description = "JSON Web Keys for signature verification")
    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Object> get() {
        return jwkSet.toJSONObject(true);
    }
}
