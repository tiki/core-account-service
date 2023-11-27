/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.cleanroom;

import com.mytiki.account.features.latest.provider.ProviderDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CleanroomRepository extends JpaRepository<CleanroomDO, Long> {
    Optional<CleanroomDO> findByCleanroomId(UUID cleanroomId);
    void deleteByCleanroomId(UUID cleanroomId);
    @Query("SELECT a FROM CleanroomDO a INNER JOIN a.org o INNER JOIN o.profiles u WHERE a.cleanroomId = :cleanroomId AND u.userId = :userId")
    Optional<CleanroomDO> findByCleanroomIdAndUserId(@Param("cleanroomId") UUID cleanroomId, @Param("userId") UUID userId);

}
