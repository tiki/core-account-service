/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.facade;

import java.util.Base64;

public class B64F {

    public static String encode(byte[] src) {
        return encode(src, false);
    }

    public static String encode(byte[] src, boolean isUrl){
        return isUrl ?
                Base64.getUrlEncoder().withoutPadding().encodeToString(src) :
                Base64.getEncoder().encodeToString(src);
    }

    public static byte[] decode(String src){
        return decode(src, false);
    }

    public static byte[] decode(byte[] src){
        return decode(src, false);
    }

    public static byte[] decode(String src, boolean isUrl){
        return isUrl ?
                Base64.getUrlDecoder().decode(src) :
                Base64.getDecoder().decode(src);
    }

    public static byte[] decode(byte[] src, boolean isUrl){
        return isUrl ?
                Base64.getUrlDecoder().decode(src) :
                Base64.getDecoder().decode(src);
    }
}
