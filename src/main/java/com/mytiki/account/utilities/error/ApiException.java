/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.error;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ApiException extends RuntimeException{
    private final HttpStatus httpStatus;
    private String id;
    private String message;
    private String detail;
    private String help;
    private Map<String, String> properties;

    public ApiException(HttpStatus httpStatus) {
        super(httpStatus.value() + "-" + httpStatus.getReasonPhrase());
        this.httpStatus = httpStatus;
    }

    public ApiException(HttpStatus httpStatus, Throwable cause) {
        super(httpStatus.value() + "-" + httpStatus.getReasonPhrase(), cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
