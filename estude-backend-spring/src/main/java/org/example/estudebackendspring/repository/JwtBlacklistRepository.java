package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.JwtBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JwtBlacklistRepository extends JpaRepository<JwtBlacklist, Long> {
    Optional<JwtBlacklist> findByToken(String token);

    // Optionally delete expired entries (can be called by scheduled job)
    void deleteByExpiresAtBefore(java.time.LocalDateTime time);
}
