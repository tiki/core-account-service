package com.mytiki.account.features.latest.addr_reg;

import com.mytiki.account.features.latest.app_info.AppInfoDO;
import com.mytiki.account.features.latest.app_info.AppInfoService;
import com.mytiki.account.features.latest.jwks.JwksDO;
import com.mytiki.account.features.latest.jwks.JwksService;
import com.mytiki.account.utilities.facade.B64F;
import com.mytiki.account.utilities.facade.RsaF;
import com.mytiki.account.utilities.facade.Sha3F;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.codec.Utf8;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AddrRegService {
    private final AddrRegRepository repository;
    private final AppInfoService appInfoService;
    private final JwksService jwksService;

    public AddrRegService(
            AddrRegRepository repository,
            AppInfoService appInfoService,
            JwksService jwksService) {
        this.repository = repository;
        this.appInfoService = appInfoService;
        this.jwksService = jwksService;
    }

    public AddrRegAORsp register(String appId, AddrRegAOReq req, String custAuth) {
        guardSignature(req);

        Optional<AppInfoDO> app = appInfoService.getDO(appId);
        if(app.isEmpty())
            throw new ApiExceptionBuilder(HttpStatus.BAD_REQUEST)
                    .message("Failed to register address")
                    .detail("Invalid App ID")
                    .help("Check your Authorization token")
                    .build();
        guardCustomerToken(custAuth, app.get().getJwks(), req);

        AddrRegDO reg = new AddrRegDO();
        reg.setCid(req.getId());
        reg.setAddress(B64F.decode(req.getAddress(), true));
        reg.setApp(app.get());
        reg.setPubKey(B64F.decode(req.getPubKey()));
        reg.setCreated(ZonedDateTime.now());

        try {
            AddrRegDO saved = repository.save(reg);
            return toRsp(saved);
        }catch(DataIntegrityViolationException e){
            throw new ApiExceptionBuilder(HttpStatus.BAD_REQUEST)
                    .message("Failed to register address")
                    .detail("Duplicate registration")
                    .build();
        }
    }

    public AddrRegAORsp get(String appId, String address){
        byte[] addr = B64F.decode(address, true);
        UUID app = UUID.fromString(appId);
        Optional<AddrRegDO> found = repository.findByAppAppIdAndAddress(app, addr);
        return found.map(this::toRsp).orElse(new AddrRegAORsp());
    }

    public List<AddrRegAORsp> getAll(String appId, String id){
        UUID app = UUID.fromString(appId);
        List<AddrRegDO> found = repository.findByAppAppIdAndCid(app, id);
        return found.stream().map(this::toRsp).toList();
    }

    public void delete(String appId, String address){
        UUID app = UUID.fromString(appId);
        byte[] addr = B64F.decode(address, true);
        repository.deleteByAppAppIdAndAddress(app, addr);
    }

    public void deleteAll(String appId, String id){
        UUID app = UUID.fromString(appId);
        repository.deleteByAppAppIdAndCid(app, id);
    }

    private void guardSignature(AddrRegAOReq req) {
        try {
            byte[] message = Utf8.encode(req.getId() + "." + req.getAddress());
            byte[] pubKey = B64F.decode(req.getPubKey());
            byte[] signature = B64F.decode(req.getSignature());

            String address = B64F.encode(Sha3F.h256(pubKey), true);
            if(!address.equals(req.getAddress()))
                throw new ApiExceptionBuilder(HttpStatus.UNAUTHORIZED)
                        .message("Failed to validate signature")
                        .detail("Invalid public key")
                        .help("B64Url(SHA3_256(pubkey)) should equal address")
                        .build();

            RSAPublicKey rsaKey = RsaF.decodePublicKey(pubKey);
            boolean isValid = RsaF.verify(rsaKey, message, signature);
            if(!isValid)
                throw new ApiExceptionBuilder(HttpStatus.UNAUTHORIZED)
                        .message("Failed to validate signature")
                        .detail("Signature does not match request")
                        .help("Format your unsigned message as: id.message")
                        .build();
        } catch (IOException | IllegalArgumentException e) {
            throw new ApiExceptionBuilder(HttpStatus.UNAUTHORIZED)
                    .message("Failed to validate signature")
                    .detail("Invalid encoding")
                    .cause(e.getCause())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new ApiExceptionBuilder(HttpStatus.UNPROCESSABLE_ENTITY)
                    .message("Failed to validate signature")
                    .detail("Something went wrong internally")
                    .help("Contact support")
                    .cause(e.getCause())
                    .build();
        }
    }

    private void guardCustomerToken(String authorization, JwksDO jwks, AddrRegAOReq req){
        if(jwks == null)
            throw new ApiExceptionBuilder(HttpStatus.FORBIDDEN)
                    .detail("Missing required JWKS endpoint")
                    .build();
        String token = authorization.replace("Bearer ", "");
        jwksService.guard(jwks.getEndpoint(), token, req.getId());
    }

    private AddrRegAORsp toRsp(AddrRegDO reg){
        AddrRegAORsp rsp = new AddrRegAORsp();
        rsp.setId(reg.getCid());
        rsp.setAddress(B64F.encode(reg.getAddress(), true));
        rsp.setPubKey(B64F.encode(reg.getPubKey()));
        rsp.setCreated(reg.getCreated());
        return rsp;
    }
}
