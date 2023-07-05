/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.api_key;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKeyDO, UUID> {
    List<ApiKeyDO> findAllByAppAppId(UUID appId);
    Optional<ApiKeyDO> findByAppAppIdAndId(UUID appId, UUID id);
}
