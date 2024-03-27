/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.provider.ProviderRepository;
import com.mytiki.account.features.latest.provider_user.ProviderUserAOReq;
import com.mytiki.account.features.latest.provider_user.ProviderUserAORsp;
import com.mytiki.account.features.latest.provider_user.ProviderUserService;
import com.mytiki.account.features.latest.provider.ProviderAO;
import com.mytiki.account.features.latest.provider.ProviderService;
import com.mytiki.account.features.latest.oauth.OauthScopes;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.oauth.OauthSubNamespace;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.fixtures.AddrFixture;
import com.mytiki.account.main.App;
import com.mytiki.account.mocks.SqsMock;
import com.mytiki.account.utilities.facade.B64F;
import com.mytiki.account.utilities.facade.SqsF;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.stripe.exception.StripeException;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProviderUserTest {

    @Autowired
    private ProviderUserService service;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private OauthScopes allowedScopes;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private JWSSigner jwsSigner;

    @Autowired
    private ObjectMapper mapper;

    private ProviderService providerService;

    @BeforeAll
    public void before() throws StripeException {
        SqsF trail = new SqsF(SqsMock.mock("dummy"), "dummy");
        providerService = new ProviderService(providerRepository, profileService, jwsSigner, trail, mapper);
    }

    @Test
    public void Test_RegisterNew_Success() throws JOSEException, NoSuchAlgorithmException, CryptoException {
        ProfileDO user = profileService.createIfNotExists(UUID.randomUUID() + "@test.com");
        ProviderAO app = providerService.create(UUID.randomUUID().toString(), new OauthSub(OauthSubNamespace.USER, user.getUserId().toString()));

        String id = UUID.randomUUID().toString();
        ProviderUserAOReq req = AddrFixture.req(id);
        ProviderUserAORsp rsp = service.register(app.getProviderId(), req);

        assertEquals(rsp.getAddress(), req.getAddress());
        assertEquals(rsp.getPubKey(), req.getPubKey());
        assertEquals(rsp.getId(), req.getId());
        assertNotNull(rsp.getCreated());
    }

    @Test
    public void Test_RegisterTwo_Success() throws JOSEException, NoSuchAlgorithmException, CryptoException {
        ProfileDO user = profileService.createIfNotExists(UUID.randomUUID() + "@test.com");
        ProviderAO app = providerService.create(
                UUID.randomUUID().toString(),
                new OauthSub(OauthSubNamespace.USER, user.getUserId().toString()));

        String id = UUID.randomUUID().toString();
        ProviderUserAOReq req1 = AddrFixture.req(id);
        ProviderUserAORsp rsp1 = service.register(app.getProviderId(), req1);
        ProviderUserAOReq req2 = AddrFixture.req(id);
        ProviderUserAORsp rsp2 = service.register(app.getProviderId(), req2);

        assertEquals(rsp1.getId(), rsp2.getId());
    }

    @Test
    public void Test_Get_Success() throws JOSEException, NoSuchAlgorithmException, CryptoException {
        ProfileDO user = profileService.createIfNotExists(UUID.randomUUID() + "@test.com");
        ProviderAO app = providerService.create(
                UUID.randomUUID().toString(),
                new OauthSub(OauthSubNamespace.USER, user.getUserId().toString()));

        String id = UUID.randomUUID().toString();
        ProviderUserAOReq req = AddrFixture.req(id);
        service.register(app.getProviderId(), req);
        ProviderUserAORsp rsp = service.get(app.getProviderId(), req.getAddress());

        assertEquals(rsp.getAddress(), req.getAddress());
        assertEquals(rsp.getPubKey(), req.getPubKey());
        assertEquals(rsp.getId(), req.getId());
        assertNotNull(rsp.getCreated());
    }

    @Test
    public void Test_GetAll_Success() throws NoSuchAlgorithmException, CryptoException, JOSEException {
        ProfileDO user = profileService.createIfNotExists(UUID.randomUUID() + "@test.com");
        ProviderAO app = providerService.create(
                UUID.randomUUID().toString(),
                new OauthSub(OauthSubNamespace.USER, user.getUserId().toString()));

        String id = UUID.randomUUID().toString();
        service.register(app.getProviderId(), AddrFixture.req(id));
        service.register(app.getProviderId(), AddrFixture.req(id));

        List<ProviderUserAORsp> rspList = service.getAll(app.getProviderId(), id);
        assertEquals(rspList.size(), 2);
    }

    @Test
    @Transactional
    public void Test_Delete_Success() throws NoSuchAlgorithmException, CryptoException, JOSEException {
        ProfileDO user = profileService.createIfNotExists(UUID.randomUUID() + "@test.com");
        ProviderAO app = providerService.create(UUID.randomUUID().toString(), new OauthSub(OauthSubNamespace.USER, user.getUserId().toString()));

        String id = UUID.randomUUID().toString();
        ProviderUserAOReq req = AddrFixture.req(id);
        service.register(app.getProviderId(), req);
        service.delete(app.getProviderId(), req.getAddress());
        ProviderUserAORsp rsp = service.get(app.getProviderId(), req.getAddress());
        assertNull(rsp.getId());
    }

    @Test
    @Transactional
    public void Test_DeleteAll_Success() throws NoSuchAlgorithmException, CryptoException, JOSEException {
        ProfileDO user = profileService.createIfNotExists(UUID.randomUUID() + "@test.com");
        ProviderAO app = providerService.create(
                UUID.randomUUID().toString(),
                new OauthSub(OauthSubNamespace.USER, user.getUserId().toString()));

        String id = UUID.randomUUID().toString();
        service.register(app.getProviderId(), AddrFixture.req(id));
        service.register(app.getProviderId(), AddrFixture.req(id));

        service.deleteAll(app.getProviderId(), id);
        List<ProviderUserAORsp> rsp = service.getAll(app.getProviderId(), id);
        assertTrue(rsp.isEmpty());
    }

    @Test
    @Transactional
    public void Test_Authorize_Success() throws NoSuchAlgorithmException, CryptoException, JOSEException {
        ProfileDO user = profileService.createIfNotExists(UUID.randomUUID() + "@test.com");
        ProviderAO app = providerService.create(
                UUID.randomUUID().toString(),
                new OauthSub(OauthSubNamespace.USER, user.getUserId().toString()));

        RSAKey keypair = new RSAKeyGenerator(RSAKeyGenerator.MIN_KEY_SIZE_BITS).generate();

        String id = UUID.randomUUID().toString();
        ProviderUserAOReq req = AddrFixture.req(id, keypair);
        ProviderUserAORsp addr = service.register(app.getProviderId(), req);

        RSAPrivateKey key = new RSAPrivateKey(
                keypair.getModulus().decodeToBigInteger(),
                keypair.getPublicExponent().decodeToBigInteger(),
                keypair.getPrivateExponent().decodeToBigInteger(),
                keypair.getFirstPrimeFactor().decodeToBigInteger(),
                keypair.getSecondPrimeFactor().decodeToBigInteger(),
                keypair.getFirstFactorCRTExponent().decodeToBigInteger(),
                keypair.getSecondFactorCRTExponent().decodeToBigInteger(),
                keypair.getFirstCRTCoefficient().decodeToBigInteger());

        RSADigestSigner signer = new RSADigestSigner(new SHA256Digest());
        RSAKeyParameters keyParams =
                new RSAKeyParameters(true, key.getModulus(), key.getPrivateExponent());
        signer.init(true, keyParams);
        byte[] message = addr.getAddress().getBytes();
        signer.update(message, 0, message.length);
        byte[] sig = signer.generateSignature();

        OAuth2AccessTokenResponse token = service.authorize(
                allowedScopes.filter("trail"),
                new OauthSub(OauthSubNamespace.ADDRESS, app.getProviderId() + ":" + addr.getAddress()),
                B64F.encode(sig));

        assertNotNull(token.getAccessToken().getTokenValue());
    }
}
