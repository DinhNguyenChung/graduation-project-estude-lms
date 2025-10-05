package org.example.estudebackendspring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.estudebackendspring.enums.NotificationPriority;
import org.example.estudebackendspring.enums.NotificationType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateNotificationRequest {
    @NotBlank
    private String message;

    private NotificationType type = NotificationType.GENERAL;
    private NotificationPriority priority = NotificationPriority.MEDIUM;
}
