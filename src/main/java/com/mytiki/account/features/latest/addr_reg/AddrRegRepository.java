package com.mytiki.account.features.latest.addr_reg;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddrRegRepository extends JpaRepository<AddrRegDO, UUID> {
    Optional<AddrRegDO> findByAppAppIdAndAddress(UUID appId, byte[] address);
    List<AddrRegDO> findByAppAppIdAndCid(UUID appId, String cid);
    void deleteByAppAppIdAndCid(UUID appId, String cid);
    void deleteByAppAppIdAndAddress(UUID appId, byte[] address);
}
