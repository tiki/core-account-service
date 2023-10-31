/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.builder;

import com.mytiki.account.utilities.error.ApiError;
import com.mytiki.account.utilities.error.ApiException;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class ErrorBuilder {
    private final HttpStatus httpStatus;
    private String id;
    private String message;
    private String detail;
    private String help;
    private Throwable cause;
    private Map<String, String> properties;

    public ErrorBuilder(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public ErrorBuilder message(String message) {
        this.message = message;
        return this;
    }

    public ErrorBuilder id(String id){
        this.id = id;
        return this;
    }

    public ErrorBuilder detail(String detail){
        this.detail = detail;
        return this;
    }

    public ErrorBuilder help(String help){
        this.help = help;
        return this;
    }

    public ErrorBuilder cause(Throwable cause){
        this.cause = cause;
        return this;
    }

    public ErrorBuilder properties(Map<String, String> properties){
        this.properties = properties;
        return  this;
    }

    public ErrorBuilder properties(String... kvPairs){
        int mapSize = kvPairs.length / 2;
        HashMap<String, String> propertiesMap = new HashMap<>(mapSize);
        for(int i=0; i<kvPairs.length; i+=2)
            propertiesMap.put(kvPairs[i], kvPairs[i+1]);
        this.properties = propertiesMap;
        return this;
    }

    public ApiException exception(){
        ApiException exception = new ApiException(httpStatus, cause);
        exception.setMessage(message);
        exception.setId(id);
        exception.setDetail(detail);
        exception.setHelp(help);
        exception.setProperties(properties);
        return exception;
    }

    public ApiError error() {
        ApiError error = new ApiError();
        error.setDetail(detail);
        error.setHelp(help);
        error.setId(id);
        error.setProperties(properties);
        error.setMessage(message);
        return error;
    }
}
