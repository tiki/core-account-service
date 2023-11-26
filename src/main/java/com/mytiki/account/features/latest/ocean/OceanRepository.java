/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OceanRepository extends JpaRepository<OceanDO, Long> {
    Optional<OceanDO> findByRequestId(UUID requestId);
}
