package com.mytiki.account.utilities.builder;

import com.mytiki.account.features.latest.jwks.JwksConfig;
import com.mytiki.account.security.oauth.OauthConfig;
import com.nimbusds.jose.*;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

public class JwtBuilder {
    private String sub;
    private List<String> aud;
    private String jti;
    private Date exp;
    private Date iat;
    private Date nbf;
    private List<String> scp;
    private List<String> roles;
    private List<String> groups;
    private Map<String, String> custom;
    private JWSObject jws;
    private String refresh;

    public JwtBuilder iat(ZonedDateTime datetime){
        iat = Date.from(datetime.toInstant());
        return this;
    }

    public JwtBuilder exp(Long seconds){
        exp = Date.from(ZonedDateTime.now().plusSeconds(seconds).toInstant());
        return this;
    }

    public JwtBuilder exp(ZonedDateTime datetime){
        exp = Date.from(datetime.toInstant());
        return this;
    }

    public JwtBuilder nbf(ZonedDateTime datetime){
        nbf = Date.from(datetime.toInstant());
        return this;
    }

    public JwtBuilder jti(String id){
        jti = id;
        return this;
    }

    public JwtBuilder sub(String subject){
        sub = subject;
        return this;
    }

    public JwtBuilder scp(List<String> scopes){
        if(scopes != null && !scopes.isEmpty())
            scp = scopes;
        return this;
    }

    public JwtBuilder aud(List<String> audiences){
        if(audiences != null && !audiences.isEmpty()) {
            if(aud == null) aud = audiences;
            else {
                List<String> list = new ArrayList<>(aud);
                list.addAll(audiences);
                aud = list;
            }
        }
        return this;
    }

    public JwtBuilder aud(String audience){
        if(audience != null) {
            if(aud == null) aud = List.of(audience);
            else {
                List<String> list = new ArrayList<>(aud);
                list.add(audience);
                aud = list;
            }
        }
        return this;
    }

    public JwtBuilder roles(List<String> roles){
        if(roles != null && !roles.isEmpty()) {
            if(this.roles == null) this.roles = roles;
            else {
                List<String> list = new ArrayList<>(this.roles);
                list.addAll(roles);
                this.roles = list;
            }
        }
        return this;
    }

    public JwtBuilder roles(String role){
        if(role != null) {
            if(this.roles == null) this.roles = List.of(role);
            else {
                List<String> list = new ArrayList<>(this.roles);
                list.add(role);
                this.roles = list;
            }
        }
        return this;
    }

    public JwtBuilder groups(List<String> groups){
        if(groups != null && !groups.isEmpty()) {
            if(this.groups == null) this.groups = groups;
            else {
                List<String> list = new ArrayList<>(this.groups);
                list.addAll(groups);
                this.groups = list;
            }
        }
        return this;
    }

    public JwtBuilder groups(String group){
        if(group != null) {
            if(this.groups == null) this.groups = List.of(group);
            else {
                List<String> list = new ArrayList<>(this.groups);
                list.add(group);
                this.groups = list;
            }
        }
        return this;
    }

    public JwtBuilder refresh(String token){
        refresh = token;
        return this;
    }

    public JwtBuilder claim(String claim, String value){
        if(claim != null && value != null) {
            if(custom == null) custom = new HashMap<>(){{ put(claim, value); }};
            else {
                Map<String, String> map = new HashMap<>(custom);
                map.put(claim, value);
                custom = map;
            }
        }
        return this;
    }

    public JwtBuilder build() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        if(iat == null) iat = Date.from(now.toInstant());
        jws = new JWSObject(
                new JWSHeader
                        .Builder(JwksConfig.ALGORITHM)
                        .type(JOSEObjectType.JWT)
                        .keyID(JwksConfig.keyId)
                        .build(),
                new Payload( new JWTClaimsSet.Builder()
                        .issuer(OauthConfig.issuer)
                        .issueTime(iat)
                        .subject(sub)
                        .expirationTime(exp)
                        .jwtID(jti)
                        .audience(aud)
                        .notBeforeTime(nbf)
                        .claim("roles", roles)
                        .claim("groups", groups)
                        .claim("scp", scp)
                        .build()
                        .toJSONObject()));
        return this;
    }

    public JwtBuilder sign(JWSSigner signer) throws JOSEException {
        jws.sign(signer);
        return this;
    }

    public OAuth2AccessTokenResponse toResponse() {
        OAuth2AccessTokenResponse.Builder builder = OAuth2AccessTokenResponse
                .withToken(jws.serialize())
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .scopes(new HashSet<>(scp));

        if(exp != null){
            long expInMilli = exp.getTime() - ZonedDateTime.now().toInstant().toEpochMilli();
            builder.expiresIn(expInMilli / 1000);
        }
        if(refresh != null) builder.refreshToken(refresh);
        return builder.build();
    }

    public String toToken() {
        return jws.serialize();
    }
}
