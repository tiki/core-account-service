/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.otp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpDO, String> {
    Optional<OtpDO> findByOtpHashed(String hashedOtp);

    Page<OtpDO> findAllByExpiresBefore(ZonedDateTime before, Pageable pageable);
}
