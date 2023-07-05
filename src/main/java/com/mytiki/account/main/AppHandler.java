package com.mytiki.account.main;

import com.mytiki.spring_rest_api.ApiError;
import com.mytiki.spring_rest_api.ApiExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Order
@ControllerAdvice
public class AppHandler extends ApiExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleException(IllegalArgumentException e, HttpServletRequest request) {
        logger.trace("Request: " + request.getRequestURI(), e);
        ApiError error = new ApiError();
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
