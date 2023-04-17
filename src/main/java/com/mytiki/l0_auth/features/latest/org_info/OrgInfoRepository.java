/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_auth.features.latest.org_info;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrgInfoRepository extends JpaRepository<OrgInfoDO, UUID> {
    Optional<OrgInfoDO> findByOrgId(UUID orgId);
}
