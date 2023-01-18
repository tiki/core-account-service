/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.otp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "otp")
public class OtpDO implements Serializable {
    private String otpHashed;
    private ZonedDateTime issued;
    private ZonedDateTime expires;

    private String email;

    @Id
    @Column(name = "otp_hashed")
    public String getOtpHashed() {
        return otpHashed;
    }

    public void setOtpHashed(String otpHashed) {
        this.otpHashed = otpHashed;
    }

    @Column(name = "issued_utc")
    public ZonedDateTime getIssued() {
        return issued;
    }

    public void setIssued(ZonedDateTime issued) {
        this.issued = issued;
    }

    @Column(name = "expires_utc")
    public ZonedDateTime getExpires() {
        return expires;
    }

    public void setExpires(ZonedDateTime expires) {
        this.expires = expires;
    }

    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
