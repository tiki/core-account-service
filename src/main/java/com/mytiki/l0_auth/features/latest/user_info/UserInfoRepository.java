/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.user_info;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserInfoRepository extends JpaRepository<UserInfoDO, Long> {
    Optional<UserInfoDO> findByUserId(UUID userId);
    Optional<UserInfoDO> findByEmail(String email);
}
