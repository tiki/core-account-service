/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.api_key;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKeyDO, Long> {
    void deleteByToken(String token);
    Optional<ApiKeyDO> findByTokenAndUserUserId(String token, UUID userId);
    List<ApiKeyDO> findAllByUserEmail(String email);
}
