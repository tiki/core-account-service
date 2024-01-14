/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.refresh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshRepository extends JpaRepository<RefreshDO, UUID> {
    Optional<RefreshDO> findByJti(UUID jti);

    @Modifying
    @Transactional
    void deleteByJti(UUID jti);

    List<RefreshDO> findAllByExpiresBefore(ZonedDateTime before);
}
