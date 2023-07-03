/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities;

import com.mytiki.spring_rest_api.ApiConstants;

public interface Constants {
    String MODULE_DOT_PATH = "com.mytiki.account";
    String MODULE_SLASH_PATH = "com/mytiki/account";

    String PROJECT_DASH_PATH = "account";

    String SLICE_FEATURES = "features";
    String SLICE_LATEST = "latest";

    String PACKAGE_FEATURES_LATEST_DOT_PATH = MODULE_DOT_PATH + "." + SLICE_FEATURES + "." + SLICE_LATEST;
    String PACKAGE_FEATURES_LATEST_SLASH_PATH = MODULE_SLASH_PATH + "/" + SLICE_FEATURES + "/" + SLICE_LATEST;

    String API_DOCS_PATH = "/v3/api-docs.yaml";

    Long TOKEN_EXPIRY_DURATION_SECONDS = 600L;
    Long REFRESH_EXPIRY_DURATION_SECONDS = 2592000L;

    String OAUTH_PATH = "oauth";
    String OAUTH_TOKEN_PATH = OAUTH_PATH + "/token";
    String OAUTH_REVOKE_PATH = OAUTH_PATH + "/revoke";

    String BASE_ROUTE = ApiConstants.API_LATEST_ROUTE + "account/";
}