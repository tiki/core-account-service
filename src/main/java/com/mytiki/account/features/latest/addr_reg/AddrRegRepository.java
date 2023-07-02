package com.mytiki.account.features.latest.addr_reg;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddrRegRepository extends JpaRepository<AddrRegDO, UUID> {}
