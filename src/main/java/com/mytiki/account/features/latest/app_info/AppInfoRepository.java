/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.app_info;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppInfoRepository extends JpaRepository<AppInfoDO, Long> {
    Optional<AppInfoDO> findByAppId(UUID apiId);
}
