package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Lấy token mới nhất (chưa used) cho email/phone
     */
    @Query("SELECT p FROM PasswordResetToken p " +
            "WHERE p.emailOrPhone = :emailOrPhone AND p.used = false " +
            "ORDER BY p.createdAt DESC")
    Optional<PasswordResetToken> findLatestActiveByEmailOrPhone(@Param("emailOrPhone") String emailOrPhone);

    /**
     * (Tùy chọn) Đánh dấu tất cả token chưa dùng của email là used (sau reset) hoặc xóa.
     * Bạn có thể gọi nó để invalidate token cũ.
     */
    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.emailOrPhone = :emailOrPhone AND p.id <> :exceptId")
    void markOtherTokensAsUsed(@Param("emailOrPhone") String emailOrPhone, @Param("exceptId") Long exceptId);


}
