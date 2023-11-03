/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities;

public interface Constants {
    String PROJECT_DASH_PATH = "account";

    String SLICE_FEATURES = "features";
    String SLICE_LATEST = "latest";

    String MODULE_DOT_PATH = "com.mytiki.account";
    String PKG_FEAT_LATEST_DOT_PATH = MODULE_DOT_PATH + "." + SLICE_FEATURES + "." + SLICE_LATEST;

    String MODULE_SLASH_PATH = "com/mytiki/account";
    String PKG_FEAT_LATEST_SLASH_PATH = MODULE_SLASH_PATH + "/" + SLICE_FEATURES + "/" + SLICE_LATEST;

    Long TOKEN_EXPIRY_DURATION_SECONDS = 600L;
    Long REFRESH_EXPIRY_DURATION_SECONDS = 2592000L;

    String AUTH_PATH = "auth";
    String AUTH_TOKEN_PATH = AUTH_PATH + "/token";
    String AUTH_REVOKE_PATH = AUTH_PATH + "/revoke";

    String API = "api";
    String LATEST = "latest";
    String API_LATEST_ROUTE = "/" + API + "/" + LATEST + "/";
    String API_DOCS_ROUTE = "/v3/api-docs.yaml";
}
