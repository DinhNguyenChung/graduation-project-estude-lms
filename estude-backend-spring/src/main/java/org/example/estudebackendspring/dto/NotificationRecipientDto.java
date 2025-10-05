package org.example.estudebackendspring.dto;

import lombok.*;
import org.example.estudebackendspring.enums.NotificationPriority;
import org.example.estudebackendspring.enums.NotificationTargetType;
import org.example.estudebackendspring.enums.NotificationType;

import java.time.LocalDateTime;
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecipientDto {
    private Long notificationRecipientId;
    private Long notificationId;
    private String message;
    private LocalDateTime sentAt;
    private Boolean read;
    private UserDTO sender;
    private NotificationType type;
    private NotificationPriority priority;
    private NotificationTargetType targetType;
    private Long targetId;
}
