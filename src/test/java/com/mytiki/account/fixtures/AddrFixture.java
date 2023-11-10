/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.fixtures;

import com.mytiki.account.features.latest.addr_reg.AddrRegAOReq;
import com.mytiki.account.utilities.facade.B64F;
import com.mytiki.account.utilities.facade.Sha3F;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.springframework.security.crypto.codec.Utf8;

import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;

public interface AddrFixture {

    static AddrRegAOReq req(String id) throws JOSEException, NoSuchAlgorithmException, CryptoException {
        RSAKey keypair = new RSAKeyGenerator(RSAKeyGenerator.MIN_KEY_SIZE_BITS).generate();
        return req(id, keypair);
    }

    static AddrRegAOReq req(String id, RSAKey keypair) throws JOSEException, NoSuchAlgorithmException, CryptoException {
        RSAPublicKey publicKey = keypair.toRSAPublicKey();
        String address = B64F.encode(Sha3F.h256(publicKey.getEncoded()), true);
        byte[] message = Utf8.encode(id + "." + address);
        RSADigestSigner signer = new RSADigestSigner(new SHA256Digest());
        signer.init(true, new RSAKeyParameters(true,
                keypair.toRSAPrivateKey().getModulus(), keypair.toRSAPrivateKey().getPrivateExponent()));
        signer.update(message, 0, message.length);
        String signature = B64F.encode(signer.generateSignature());
        return new AddrRegAOReq(id, address, B64F.encode(publicKey.getEncoded()), signature);
    }

}
