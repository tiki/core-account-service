/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.error;

import java.util.Map;
import java.util.Objects;

public class ApiError {
    private String id;
    private String message;
    private String detail;
    private String help;
    private Map<String, String> properties;

    public ApiError() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    @Override
    public String toString() {
        return "ApiError{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", detail='" + detail + '\'' +
                ", help='" + help + '\'' +
                ", properties=" + properties +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiError apiError = (ApiError) o;
        return Objects.equals(id, apiError.id) && Objects.equals(message, apiError.message) && Objects.equals(detail, apiError.detail) && Objects.equals(help, apiError.help) && Objects.equals(properties, apiError.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, detail, help, properties);
    }
}
