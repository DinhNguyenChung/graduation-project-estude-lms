package org.example.estudebackendspring.service;


import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.config.JwtTokenUtil;
import org.example.estudebackendspring.entity.JwtBlacklist;
import org.example.estudebackendspring.repository.JwtBlacklistRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JwtBlacklistService {

    private final JwtBlacklistRepository jwtBlacklistRepository;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Add token to blacklist. expiresAt should be token expiry time to enable cleanup later.
     */
    public void blacklistToken(String token) {
        // If token already blacklisted, do nothing
        if (jwtBlacklistRepository.findByToken(token).isPresent()) return;

        // Parse expiry from token
        java.util.Date exp = jwtTokenUtil.getAllClaimsFromToken(token).getExpiration();
        LocalDateTime expiresAt = exp.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();

        JwtBlacklist entry = JwtBlacklist.builder()
                .token(token)
                .blacklistedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();

        jwtBlacklistRepository.save(entry);
    }

    public boolean isBlacklisted(String token) {
        return jwtBlacklistRepository.findByToken(token).isPresent();
    }

    /**
     * Optional maintenance: remove expired blacklist entries.
     */
    public void removeExpired() {
        jwtBlacklistRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}