/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.facade;

import java.security.SecureRandom;

public class RandF {
    public static String create(int bytes) {
        byte[] secret = new byte[bytes];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(secret);
        return B64F.encode(secret);
    }
}
