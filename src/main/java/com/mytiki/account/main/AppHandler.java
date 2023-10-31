/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.main;

import com.mytiki.account.utilities.error.ApiError;
import com.mytiki.account.utilities.error.ApiException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.lang.invoke.MethodHandles;

@Order
@ControllerAdvice
public class AppHandler {
    protected static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ApiError> handleException(Exception e, HttpServletRequest request){
        logger.error("Request: " + request.getRequestURI(), e);
        ApiError error = new ApiError();
        error.setMessage(e.getMessage());
        error.setDetail(e.getClass().getName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(value = {NoHandlerFoundException.class})
    public ResponseEntity<ApiError> handleNoHandlerFoundException(
            NoHandlerFoundException e, HttpServletRequest request){
        logger.trace("Request: " + request.getRequestURI(), e);
        ApiError error = new ApiError();
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(value = {ServletException.class})
    public ResponseEntity<ApiError> handleServletException(ServletException e, HttpServletRequest request){
        logger.trace("Request: " + request.getRequestURI(), e);
        ApiError error = new ApiError();
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(value = {HttpClientErrorException.class})
    public ResponseEntity<ApiError> handleHttpClientErrorException(
            HttpClientErrorException e, HttpServletRequest request){
        String logMsg = "Request: " + request.getRequestURI();
        if (e.getStatusCode().is5xxServerError()) logger.error(logMsg, e);
        else logger.trace(logMsg, e);
        ApiError error = new ApiError();
        error.setMessage(e.getMessage());
        return ResponseEntity.status(e.getStatusCode()).body(error);
    }

    @ExceptionHandler(value = {HttpMessageConversionException.class})
    public ResponseEntity<ApiError> handleHttpMessageConversionException(
            HttpMessageConversionException e, HttpServletRequest request){
        logger.trace("Request: " + request.getRequestURI(), e);
        ApiError error = new ApiError();
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ApiError> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request){
        logger.trace("Request: " + request.getRequestURI(), e);
        ApiError error = new ApiError();
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    @ExceptionHandler(value = {ApiException.class})
    public ResponseEntity<ApiError> handleApiException(ApiException e, HttpServletRequest request){
        logger.debug("Request: " + request.getRequestURI(), e);
        ApiError error = new ApiError();
        error.setId(e.getId());
        error.setMessage(e.getMessage());
        error.setDetail(e.getDetail());
        error.setHelp(e.getHelp());
        error.setProperties(e.getProperties());
        return ResponseEntity.status(e.getHttpStatus()).body(error);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleException(IllegalArgumentException e, HttpServletRequest request) {
        logger.trace("Request: " + request.getRequestURI(), e);
        ApiError error = new ApiError();
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
