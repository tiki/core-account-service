/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.oauth;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.lang.invoke.MethodHandles;

public class OauthExecHandler {
    protected static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @ExceptionHandler({OAuth2AuthorizationException.class})
    public ResponseEntity<OAuth2Error> handle(OAuth2AuthorizationException e, HttpServletRequest request) {
        logger.error("Request: " + request.getRequestURI(), e);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (e.getError().getErrorCode().equals(OAuth2ErrorCodes.INVALID_CLIENT) ||
                e.getError().getErrorCode().equals(OAuth2ErrorCodes.INVALID_TOKEN))
            status = HttpStatus.UNAUTHORIZED;
        else if (e.getError().getErrorCode().equals(OAuth2ErrorCodes.INVALID_SCOPE))
            status = HttpStatus.FORBIDDEN;

        return ResponseEntity.status(status).body(e.getError());
    }
}
