package org.example.estudebackendspring.dto;

import lombok.*;
import org.example.estudebackendspring.enums.NotificationPriority;
import org.example.estudebackendspring.enums.NotificationTargetType;
import org.example.estudebackendspring.enums.NotificationType;

import java.time.LocalDateTime;
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Long notificationId;
    private String message;
    private LocalDateTime sentAt;
    private NotificationType type;
    private NotificationPriority priority;
    private NotificationTargetType targetType;
    private Long targetId;
    private Long schoolId;
    private UserDTO sender;
    private Long recipientCount;
}
