package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.PasswordResetToken;
import org.example.estudebackendspring.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    // token gần nhất của email (chưa used)
    @Query("SELECT p FROM PasswordResetToken p " +
            "WHERE p.emailOrPhone = :emailOrPhone AND p.used = false " +
            "ORDER BY p.createdAt DESC")
    Optional<PasswordResetToken> findLatestActiveByEmailOrPhone(@Param("emailOrPhone") String emailOrPhone);
    // token mới nhất theo user
    Optional<PasswordResetToken> findTopByUserOrderByCreatedAtDesc(Student user);
}
