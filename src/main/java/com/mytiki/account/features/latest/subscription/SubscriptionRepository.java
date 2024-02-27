/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.subscription;

import com.mytiki.account.features.latest.provider.ProviderDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<SubscriptionDO, Long> {
    Optional<SubscriptionDO> findBySubscriptionId(UUID subscriptionId);

    @Query("SELECT a FROM SubscriptionDO a INNER JOIN a.cleanroom c INNER JOIN c.org o INNER JOIN o.profiles u WHERE u.userId = :userId")
    List<SubscriptionDO> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT a FROM SubscriptionDO a INNER JOIN a.cleanroom c INNER JOIN c.org o INNER JOIN o.profiles u WHERE u.userId = :userId AND a.status = :status")
    List<SubscriptionDO> findByStatusAndUserId(@Param("status") SubscriptionStatus status, @Param("userId") UUID userId);

    List<SubscriptionDO> findByEventsRequestId(UUID requestId);
}
