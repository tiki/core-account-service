package com.mytiki.account.features.latest.addr_reg;

import com.mytiki.account.features.latest.app_info.AppInfoDO;
import com.mytiki.account.features.latest.app_info.AppInfoService;
import com.mytiki.account.utilities.B64Url;
import com.mytiki.account.utilities.RSAFacade;
import com.mytiki.account.utilities.SHA3Facade;
import com.mytiki.spring_rest_api.ApiExceptionBuilder;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.codec.Utf8;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AddrRegService {
    private final AddrRegRepository repository;
    private final AppInfoService appInfoService;

    public AddrRegService(
            AddrRegRepository repository,
            AppInfoService appInfoService) {
        this.repository = repository;
        this.appInfoService = appInfoService;
    }

    public AddrRegAORsp register(String appId, AddrRegAOReq req) {
        guardSignature(req);

        Optional<AppInfoDO> app = appInfoService.getDO(appId);
        if(app.isEmpty())
            throw new ApiExceptionBuilder(HttpStatus.BAD_REQUEST)
                    .message("Failed to register address")
                    .detail("Invalid App ID")
                    .help("Check your Authorization token")
                    .build();
        guardCustomerToken(app.get(), req);

        AddrRegDO reg = new AddrRegDO();
        reg.setCid(req.getId());
        reg.setAddress(B64Url.decode(req.getAddress()));
        reg.setApp(app.get());
        reg.setPubKey(Base64.getDecoder().decode(req.getPubKey()));
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
        byte[] addr = B64Url.decode(address);
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
        byte[] addr = B64Url.decode(address);
        repository.deleteByAppAppIdAndAddress(app, addr);
    }

    public void deleteAll(String appId, String id){
        UUID app = UUID.fromString(appId);
        repository.deleteByAppAppIdAndCid(app, id);
    }

    private void guardSignature(AddrRegAOReq req) {
        try {
            byte[] message = Utf8.encode(req.getId() + "." + req.getAddress());
            byte[] pubKey = Base64.getDecoder().decode(req.getPubKey());
            byte[] signature = Base64.getDecoder().decode(req.getSignature());

            String address = B64Url.encode(SHA3Facade.sha256(pubKey));
            if(!address.equals(req.getAddress()))
                throw new ApiExceptionBuilder(HttpStatus.UNAUTHORIZED)
                        .message("Failed to validate signature")
                        .detail("Invalid public key")
                        .help("B64Url(SHA3_256(pubkey)) should equal address")
                        .build();

            RSAPublicKey rsaKey = RSAFacade.decodePublicKey(pubKey);
            boolean isValid = RSAFacade.verify(rsaKey, message, signature);
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

    private void guardCustomerToken(AppInfoDO app, AddrRegAOReq req){
        if(app.getJwksEndpoint() == null) return;
        //TODO bring in reg. proj
    }

    private AddrRegAORsp toRsp(AddrRegDO reg){
        AddrRegAORsp rsp = new AddrRegAORsp();
        rsp.setId(reg.getCid());
        rsp.setAddress(B64Url.encode(reg.getAddress()));
        rsp.setPubKey(Base64.getEncoder().encodeToString(reg.getPubKey()));
        rsp.setCreated(reg.getCreated());
        return rsp;
    }
}
