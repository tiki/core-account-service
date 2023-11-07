/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@XRayEnabled
public class Sha3F {
    public static byte[] h256(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA3-256");
        return md.digest(input);
    }
}
