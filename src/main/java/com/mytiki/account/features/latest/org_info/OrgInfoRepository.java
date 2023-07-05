/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.org_info;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrgInfoRepository extends JpaRepository<OrgInfoDO, UUID> {
    Optional<OrgInfoDO> findByOrgId(UUID orgId);
    @Query("SELECT o FROM OrgInfoDO o INNER JOIN o.apps a WHERE a.appId = :appId")
    Optional<OrgInfoDO> findByAppId(@Param("appId") UUID appId);
    @Query("SELECT o FROM OrgInfoDO o INNER JOIN o.users u WHERE u.userId = :userId")
    Optional<OrgInfoDO> findByUserId(@Param("userId") UUID userId);
}
