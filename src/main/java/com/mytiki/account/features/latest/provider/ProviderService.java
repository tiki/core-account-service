/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.provider;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.features.latest.oauth.OauthScopes;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import com.mytiki.account.utilities.builder.JwtBuilder;
import com.mytiki.account.utilities.facade.B64F;
import com.mytiki.account.utilities.facade.RsaF;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@XRayEnabled
public class ProviderService {

    private final ProviderRepository repository;
    private final ProfileService profileService;
    private final JWSSigner signer;

    public ProviderService(ProviderRepository repository, ProfileService profileService, JWSSigner signer) {
        this.repository = repository;
        this.profileService = profileService;
        this.signer = signer;
    }

    public ProviderAO create(String name, OauthSub sub){
        if(!sub.isUser()) throw new ErrorBuilder(HttpStatus.UNAUTHORIZED).exception();
        Optional<ProfileDO> user =  profileService.getDO(sub.getId());
        if(user.isEmpty())
        throw new ErrorBuilder(HttpStatus.FORBIDDEN).exception();
            else {
                ZonedDateTime now = ZonedDateTime.now();
                ProviderDO app = new ProviderDO();
                app.setName(name);
                app.setProviderId(UUID.randomUUID());
                app.setOrg(user.get().getOrg());
                app.setCreated(now);
                app.setModified(now);
            try {
                RSAPrivateKey privateKey = RsaF.generate();
                app.setSignKey(privateKey);
                RSAPublicKey publicKey = RsaF.toPublic(privateKey);
                app.setPubKey(B64F.encode(publicKey.getEncoded()));
            } catch (JOSEException | IOException e) {
                throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                        .message("Issue with Sign Key")
                        .detail(e.getMessage())
                        .help("Please contact support")
                        .exception();
            }
            return toAO(repository.save(app));
        }
    }

    public ProviderAO get(String providerId){
        Optional<ProviderDO> found = repository.findByProviderId(UUID.fromString(providerId));
        return found.map(this::toAO).orElse(null);
    }

    public ProviderAO update(String providerId, ProviderAOReq req){
        Optional<ProviderDO> found = repository.findByProviderId(UUID.fromString(providerId));
        if(found.isEmpty())
            throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                    .detail("Invalid App ID")
                    .exception();
        ProviderDO update = found.get();
        update.setName(req.getName());
        update.setModified(ZonedDateTime.now());
        update = repository.save(update);
        return toAO(update);
    }

    public void delete(String providerId){
        repository.deleteByProviderId(UUID.fromString(providerId));
    }

    public OAuth2AccessTokenResponse authorize(OauthScopes scopes, OauthSub sub, String clientSecret, Long expires) {
        Optional<ProviderDO> found = repository.findByPubKeyAndProviderId(clientSecret, UUID.fromString(sub.getId()));
        if(found.isEmpty())
            throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
        try {
            return new JwtBuilder()
                    .exp(expires)
                    .sub(sub)
                    .aud(scopes.getAud())
                    .scp(scopes.getScp())
                    .build()
                    .sign(signer)
                    .toResponse();
        } catch (JOSEException e) {
            throw new OAuth2AuthorizationException(new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "Issue with JWT construction",
                    null
            ), e);
        }
    }

    public Optional<ProviderDO> getDO(String providerId){
        return repository.findByProviderId(UUID.fromString(providerId));
    }

    public void guard(JwtAuthenticationToken token, String providerId){
        if(OauthScopes.hasScope(token,"account:internal:read")) return;
        OauthSub sub = new OauthSub(token.getName());
        if (sub.isProvider() && !sub.getId().equals(providerId)) {
            throw new ErrorBuilder(HttpStatus.FORBIDDEN)
                    .detail("Invalid claim: sub")
                    .help("Provider ID does not match claim")
                    .exception();
        }else if (sub.isUser()){
            Optional<ProviderDO> app = repository.findByProviderIdAndUserId(UUID.fromString(providerId), UUID.fromString(sub.getId()));
            if (app.isEmpty())
                throw new ErrorBuilder(HttpStatus.FORBIDDEN)
                        .detail("Invalid claim: sub")
                        .help("User ID must belong to App's Org")
                        .exception();
        }
    }

    private ProviderAO toAO(ProviderDO src){
        ProviderAO rsp = new ProviderAO();
        rsp.setProviderId(src.getProviderId().toString());
        rsp.setName(src.getName());
        rsp.setModified(src.getModified());
        rsp.setCreated(src.getCreated());
        rsp.setOrgId(src.getOrg().getOrgId().toString());
        rsp.setPubKey(src.getPubKey());
        return rsp;
    }
}
