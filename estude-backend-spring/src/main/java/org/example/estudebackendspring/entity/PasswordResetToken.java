package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens",
        indexes = {
                @Index(name = "idx_password_reset_token_token", columnList = "token"),
                @Index(name = "idx_password_reset_token_email", columnList = "email_or_phone")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_password_reset_token_token", columnNames = {"token"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Lưu OTP hoặc token. Với OTP 6 chữ số thì vẫn đủ dạng String.
     * Nếu bạn muốn token dài hơn (UUID/JWT fingerprint) thì cũng để String.
     */
    @Column(nullable = false, unique = true, length = 128, name = "token")
    private String token;

    @Column(name = "email_or_phone", nullable = false)
    private String emailOrPhone;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Quan hệ với bảng Student. Nếu Student bảng khác tên, chỉnh lại import / mapping.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Student user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiryDate == null) {
            // Mặc định token hợp lệ 15 phút — thay đổi tuỳ nhu cầu
            expiryDate = LocalDateTime.now().plusMinutes(15);
        }
    }

    @Transient
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
