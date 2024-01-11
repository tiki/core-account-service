/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.api_key;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKeyDO, Long> {
    @Modifying
    @Transactional
    void deleteByToken(String token);
    Optional<ApiKeyDO> findByTokenAndProfileUserId(String token, UUID userId);
    List<ApiKeyDO> findAllByProfileEmail(String email);
}
