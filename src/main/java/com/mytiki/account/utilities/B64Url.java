/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities;

import java.util.Base64;

public class B64Url {

    public static String encode(byte[] src){
        return Base64.getUrlEncoder().withoutPadding().encodeToString(src);
    }

    public static byte[] decode(String src){
        return Base64.getUrlDecoder().decode(src);
    }
}
