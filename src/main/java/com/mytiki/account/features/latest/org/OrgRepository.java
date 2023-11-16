/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.org;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrgRepository extends JpaRepository<OrgDO, Long> {
    Optional<OrgDO> findByOrgId(UUID orgId);
    @Query("SELECT o FROM OrgDO o INNER JOIN o.providers a WHERE a.providerId = :appId")
    Optional<OrgDO> findByAppId(@Param("appId") UUID appId);
    @Query("SELECT o FROM OrgDO o INNER JOIN o.profiles u WHERE u.userId = :userId")
    Optional<OrgDO> findByUserId(@Param("userId") UUID userId);
}
