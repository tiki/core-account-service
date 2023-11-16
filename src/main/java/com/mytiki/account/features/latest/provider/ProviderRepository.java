/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.provider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProviderRepository extends JpaRepository<ProviderDO, Long> {
    Optional<ProviderDO> findByProviderId(UUID providerId);
    void deleteByProviderId(UUID providerId);
    @Query("SELECT a FROM ProviderDO a INNER JOIN a.org o INNER JOIN o.profiles u WHERE a.providerId = :providerId AND u.userId = :userId")
    Optional<ProviderDO> findByProviderIdAndUserId(@Param("providerId") UUID providerId, @Param("userId") UUID userId);

    Optional<ProviderDO> findByPubKeyAndProviderId(String pubKey, UUID providerId);
}
