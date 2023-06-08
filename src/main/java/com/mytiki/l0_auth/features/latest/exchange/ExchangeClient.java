/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.exchange;

public interface ExchangeClient {
    String validate(String tokenType, String token);
}
