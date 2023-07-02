/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA3Facade {
    public static byte[] sha256(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA3-256");
        return md.digest(input);
    }
}
