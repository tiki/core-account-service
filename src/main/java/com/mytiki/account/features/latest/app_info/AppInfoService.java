/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.app_info;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.user_info.UserInfoDO;
import com.mytiki.account.features.latest.user_info.UserInfoService;
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
public class AppInfoService {

    private final AppInfoRepository repository;
    private final UserInfoService userInfoService;
    private final JWSSigner signer;

    public AppInfoService(AppInfoRepository repository, UserInfoService userInfoService, JWSSigner signer) {
        this.repository = repository;
        this.userInfoService = userInfoService;
        this.signer = signer;
    }

    public AppInfoAO create(String name, String userId){
       Optional<UserInfoDO> user =  userInfoService.getDO(userId);
       if(user.isEmpty())
           throw new ErrorBuilder(HttpStatus.FORBIDDEN).exception();
       else {
           ZonedDateTime now = ZonedDateTime.now();
           AppInfoDO app = new AppInfoDO();
           app.setName(name);
           app.setAppId(UUID.randomUUID());
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

    public AppInfoAO get(String appId){
        Optional<AppInfoDO> found = repository.findByAppId(UUID.fromString(appId));
        return found.map(this::toAO).orElse(null);
    }

    public AppInfoAO update(String appId, AppInfoAOReq req){
        Optional<AppInfoDO> found = repository.findByAppId(UUID.fromString(appId));
        if(found.isEmpty())
            throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                    .detail("Invalid App ID")
                    .exception();
        AppInfoDO update = found.get();
        update.setName(req.getName());
        update = repository.save(update);
        return toAO(update);
    }

    public void delete(String appId){
        repository.deleteByAppId(UUID.fromString(appId));
    }

    public OAuth2AccessTokenResponse authorize(OauthScopes scopes, OauthSub sub, String clientSecret, Long expires) {
        Optional<AppInfoDO> found = repository.findByPubKeyAndAppId(clientSecret, UUID.fromString(sub.getId()));
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

    public Optional<AppInfoDO> getDO(String appId){
        return repository.findByAppId(UUID.fromString(appId));
    }

    private AppInfoAO toAO(AppInfoDO src){
        AppInfoAO rsp = new AppInfoAO();
        rsp.setAppId(src.getAppId().toString());
        rsp.setName(src.getName());
        rsp.setModified(src.getModified());
        rsp.setCreated(src.getCreated());
        rsp.setOrgId(src.getOrg().getOrgId().toString());
        rsp.setPubKey(src.getPubKey());
        return rsp;
    }

    @Deprecated
    public void guard(JwtAuthenticationToken token, String appId){
        if(OauthScopes.hasScope(token,"account:internal:read")) return;
        OauthSub sub = new OauthSub(token.getName());
        if (sub.isApp() && !sub.getId().equals(appId)) {
            throw new ErrorBuilder(HttpStatus.FORBIDDEN)
                    .detail("Invalid claim: sub")
                    .help("App ID does not match claim")
                    .exception();
        }else if (sub.isUser()){
            Optional<AppInfoDO> app = repository.findByAppIdAndUserId(UUID.fromString(appId), UUID.fromString(sub.getId()));
            if (app.isEmpty())
                throw new ErrorBuilder(HttpStatus.FORBIDDEN)
                        .detail("Invalid claim: sub")
                        .help("User ID must belong to App's Org")
                        .exception();
        }
    }
}
