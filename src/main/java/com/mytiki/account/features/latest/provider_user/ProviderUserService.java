/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.provider_user;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.provider.ProviderDO;
import com.mytiki.account.features.latest.provider.ProviderService;
import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.features.latest.oauth.OauthScopes;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.oauth.OauthSubNamespace;
import com.mytiki.account.utilities.Constants;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import com.mytiki.account.utilities.builder.JwtBuilder;
import com.mytiki.account.utilities.facade.B64F;
import com.mytiki.account.utilities.facade.RsaF;
import com.mytiki.account.utilities.facade.Sha3F;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@XRayEnabled
public class ProviderUserService {
    private final ProviderUserRepository repository;
    private final ProviderService providerService;
    private final RefreshService refreshService;
    private final JWSSigner signer;
    private final List<String> publicScopes;

    public ProviderUserService(
            ProviderUserRepository repository,
            ProviderService providerService,
            RefreshService refreshService,
            JWSSigner signer,
            List<String> publicScopes) {
        this.repository = repository;
        this.providerService = providerService;
        this.publicScopes = publicScopes;
        this.refreshService = refreshService;
        this.signer = signer;
    }

    public ProviderUserAORsp register(String providerId, ProviderUserAOReq req) {
        guardSignature(req);

        Optional<ProviderDO> app = providerService.getDO(providerId);
        if(app.isEmpty())
            throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                    .message("Failed to register address")
                    .detail("Invalid App ID")
                    .help("Check your Authorization token")
                    .exception();

        ProviderUserDO reg = new ProviderUserDO();
        reg.setCid(req.getId());
        reg.setAddress(B64F.decode(req.getAddress(), true));
        reg.setProvider(app.get());
        reg.setPubKey(B64F.decode(req.getPubKey()));
        reg.setCreated(ZonedDateTime.now());

        try {
            ProviderUserDO saved = repository.save(reg);
            return toRsp(saved);
        }catch(DataIntegrityViolationException e){
            throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                    .message("Failed to register address")
                    .detail("Duplicate registration")
                    .exception();
        }
    }

    public ProviderUserAORsp get(String providerId, String address){
        byte[] addr = B64F.decode(address, true);
        UUID app = UUID.fromString(providerId);
        Optional<ProviderUserDO> found = repository.findByProviderProviderIdAndAddress(app, addr);
        return found.map(this::toRsp).orElse(new ProviderUserAORsp());
    }

    public List<ProviderUserAORsp> getAll(String providerId, String id){
        UUID app = UUID.fromString(providerId);
        List<ProviderUserDO> found = repository.findByProviderProviderIdAndCid(app, id);
        return found.stream().map(this::toRsp).toList();
    }

    public void delete(String providerId, String address){
        UUID app = UUID.fromString(providerId);
        byte[] addr = B64F.decode(address, true);
        repository.deleteByProviderProviderIdAndAddress(app, addr);
    }

    public void deleteAll(String providerId, String id){
        UUID app = UUID.fromString(providerId);
        repository.deleteByProviderProviderIdAndCid(app, id);
    }

    public OAuth2AccessTokenResponse authorize(
            OauthScopes scopes, OauthSub sub, String clientSecret) {
        String[] split = sub.getId().split(":");
        if(split.length != 2)
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
        String providerId = split[0];
        String address = split[1];
        byte[] addr = B64F.decode(address, true);
        UUID app = UUID.fromString(providerId);
        Optional<ProviderUserDO> found = repository.findByProviderProviderIdAndAddress(app, addr);
        if(found.isPresent()){
            try {
                RSAPublicKey pubKey = RsaF.decodePublicKey(found.get().getPubKey());
                boolean isValid = RsaF.verify(
                        pubKey,
                        address.getBytes(StandardCharsets.UTF_8),
                        B64F.decode(clientSecret));
                if(isValid){
                    scopes = scopes.filter(publicScopes);
                    OauthSub subject = new OauthSub(OauthSubNamespace.ADDRESS, providerId + ":" + address);
                    return new JwtBuilder()
                            .exp(Constants.TOKEN_EXPIRY_DURATION_SECONDS)
                            .sub(subject)
                            .aud(scopes.getAud())
                            .scp(scopes.getScp())
                            .refresh(refreshService.issue(subject, scopes.getAud(), scopes.getScp()))
                            .build()
                            .sign(signer)
                            .toResponse();
                } else {
                    throw new OAuth2AuthorizationException(new OAuth2Error(
                            OAuth2ErrorCodes.ACCESS_DENIED,
                            "invalid signature",
                            null
                    ));
                }
            } catch (IOException | JOSEException e) {
                throw new OAuth2AuthorizationException(new OAuth2Error(
                        OAuth2ErrorCodes.SERVER_ERROR,
                        "Issue with JWT construction",
                        null
                ));
            }
        }else
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.ACCESS_DENIED,
                    "app-id and/or address are invalid",
                    null
            ));
    }

    public void guard(JwtAuthenticationToken token, String providerId){
        providerService.guard(token, providerId);
    }

    private void guardSignature(ProviderUserAOReq req) {
        try {
            byte[] message = Utf8.encode(req.getId() + "." + req.getAddress());
            byte[] pubKey = B64F.decode(req.getPubKey());
            byte[] signature = B64F.decode(req.getSignature());

            String address = B64F.encode(Sha3F.h256(pubKey), true);
            if(!address.equals(req.getAddress()))
                throw new ErrorBuilder(HttpStatus.UNAUTHORIZED)
                        .message("Failed to validate signature")
                        .detail("Invalid public key")
                        .help("B64Url(SHA3_256(pubkey)) should equal address")
                        .exception();

            RSAPublicKey rsaKey = RsaF.decodePublicKey(pubKey);
            boolean isValid = RsaF.verify(rsaKey, message, signature);
            if(!isValid)
                throw new ErrorBuilder(HttpStatus.UNAUTHORIZED)
                        .message("Failed to validate signature")
                        .detail("Signature does not match request")
                        .help("Format your unsigned message as: id.message")
                        .exception();
        } catch (IOException | IllegalArgumentException e) {
            throw new ErrorBuilder(HttpStatus.UNAUTHORIZED)
                    .message("Failed to validate signature")
                    .detail("Invalid encoding")
                    .cause(e.getCause())
                    .exception();
        } catch (NoSuchAlgorithmException e) {
            throw new ErrorBuilder(HttpStatus.UNPROCESSABLE_ENTITY)
                    .message("Failed to validate signature")
                    .detail("Something went wrong internally")
                    .help("Contact support")
                    .cause(e.getCause())
                    .exception();
        }
    }

    private ProviderUserAORsp toRsp(ProviderUserDO reg){
        ProviderUserAORsp rsp = new ProviderUserAORsp();
        rsp.setId(reg.getCid());
        rsp.setAddress(B64F.encode(reg.getAddress(), true));
        rsp.setPubKey(B64F.encode(reg.getPubKey()));
        rsp.setCreated(reg.getCreated());
        return rsp;
    }
}
