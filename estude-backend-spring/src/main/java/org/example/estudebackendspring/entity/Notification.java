package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.enums.NotificationPriority;
import org.example.estudebackendspring.enums.NotificationType;
import org.example.estudebackendspring.enums.UserRole;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;
    
    private String message;
    private LocalDateTime sentAt;
    
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    private Long senderId;
    
    @Enumerated(EnumType.STRING)
    private UserRole targetRole;
    
    private Boolean isSystemWide;
    
    @Enumerated(EnumType.STRING)
    private NotificationPriority priority;
      @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    // Instead, store just the user ID
//    @Column(name = "user_id")
//    private Long userId;
}
