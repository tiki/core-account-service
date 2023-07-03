/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.facade;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha3F {
    public static byte[] h256(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA3-256");
        return md.digest(input);
    }
}
