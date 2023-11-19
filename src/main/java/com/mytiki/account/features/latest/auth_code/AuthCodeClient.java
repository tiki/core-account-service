/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.auth_code;

public interface AuthCodeClient {
    String exchange(String code);
    String clientId();
}
