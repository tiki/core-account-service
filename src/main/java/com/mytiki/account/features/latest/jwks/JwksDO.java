package com.mytiki.account.features.latest.jwks;

import com.mytiki.account.utilities.converter.JWKSetConvert;
import com.mytiki.account.utilities.converter.URIConvert;
import com.nimbusds.jose.jwk.JWKSet;
import jakarta.persistence.*;

import java.io.Serializable;
import java.net.URI;
import java.time.ZonedDateTime;

@Entity
@Table(name = "jwks")
public class JwksDO implements Serializable {
    private Long id;
    private URI endpoint;
    private JWKSet keySet;
    private Boolean verifySub;
    private ZonedDateTime modified;
    private ZonedDateTime created;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jwks_id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "endpoint")
    @Convert(converter = URIConvert.class)
    public URI getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
    }

    @Column(name = "key_set")
    @Convert(converter = JWKSetConvert.class)
    public JWKSet getKeySet() {
        return keySet;
    }

    public void setKeySet(JWKSet keySet) {
        this.keySet = keySet;
    }

    @Column(name = "verify_sub")
    public Boolean getVerifySub() {
        return verifySub;
    }

    public void setVerifySub(Boolean verifySub) {
        this.verifySub = verifySub;
    }

    @Column(name = "modified_utc")
    public ZonedDateTime getModified() {
        return modified;
    }

    public void setModified(ZonedDateTime modified) {
        this.modified = modified;
    }

    @Column(name = "created_utc")
    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }
}
