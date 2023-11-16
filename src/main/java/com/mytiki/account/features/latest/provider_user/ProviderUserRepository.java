/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.provider_user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderUserRepository extends JpaRepository<ProviderUserDO, Long> {
    Optional<ProviderUserDO> findByProviderProviderIdAndAddress(UUID providerId, byte[] address);
    List<ProviderUserDO> findByProviderProviderIdAndCid(UUID providerId, String cid);
    void deleteByProviderProviderIdAndCid(UUID providerId, String cid);
    void deleteByProviderProviderIdAndAddress(UUID providerId, byte[] address);
}
