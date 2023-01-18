/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.utilities;

import com.mytiki.spring_rest_api.ApiExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class ExHandler extends ApiExceptionHandler {

    @ExceptionHandler({OAuth2AuthorizationException.class})
    public ResponseEntity<OAuth2Error> handleException(OAuth2AuthorizationException e, HttpServletRequest request) {
        logger.error("Request: " + request.getRequestURI() + "caused {}", e);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (e.getError().getErrorCode().equals(OAuth2ErrorCodes.INVALID_CLIENT) ||
                e.getError().getErrorCode().equals(OAuth2ErrorCodes.INVALID_TOKEN))
            status = HttpStatus.UNAUTHORIZED;
        else if (e.getError().getErrorCode().equals(OAuth2ErrorCodes.INVALID_SCOPE))
            status = HttpStatus.FORBIDDEN;

        return ResponseEntity.status(status).body(e.getError());
    }
}
