package org.example.estudebackendspring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.estudebackendspring.enums.NotificationPriority;
import org.example.estudebackendspring.enums.NotificationTargetType;
import org.example.estudebackendspring.enums.NotificationType;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {
    @NotBlank
    private String message;

    @NotNull
    private NotificationTargetType targetType; // SYSTEM / SCHOOL / CLASS / CLASS_SUBJECT

    // required when targetType != SYSTEM
    private Long targetId;

    private NotificationType type = NotificationType.GENERAL;
    private NotificationPriority priority = NotificationPriority.MEDIUM;
}
