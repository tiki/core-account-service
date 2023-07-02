/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.exchange;

public interface ExchangeClient {
    String validate(String tokenType, String token);
}
