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
    @Query("SELECT a FROM AppInfoDO a WHERE a.appId = ?1 AND a.org.orgId = (SELECT u.org.orgId FROM UserInfoDO u WHERE u.userId = ?2)")
    Optional<AppInfoDO> findByAppIdAndUserId(UUID appId, UUID userId);
}