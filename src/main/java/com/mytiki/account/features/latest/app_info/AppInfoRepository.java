/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.app_info;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AppInfoRepository extends JpaRepository<AppInfoDO, Long> {
    Optional<AppInfoDO> findByAppId(UUID appId);
    void deleteByAppId(UUID appId);
    @Query("SELECT a FROM AppInfoDO a INNER JOIN a.org o INNER JOIN o.users u WHERE a.appId = :appId AND u.userId = :userId")
    Optional<AppInfoDO> findByAppIdAndUserId(UUID appId, UUID userId);
}