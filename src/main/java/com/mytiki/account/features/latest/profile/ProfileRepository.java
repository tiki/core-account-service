/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<ProfileDO, Long> {
    Optional<ProfileDO> findByUserId(UUID userId);
    Optional<ProfileDO> findByEmail(String email);
}
