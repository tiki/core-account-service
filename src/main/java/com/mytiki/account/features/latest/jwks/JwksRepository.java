package com.mytiki.account.features.latest.jwks;

import org.springframework.data.jpa.repository.JpaRepository;

import java.net.URI;
import java.util.Optional;

public interface JwksRepository extends JpaRepository<JwksDO, Long> {
    Optional<JwksDO> getByEndpoint(URI endpoint);
}
