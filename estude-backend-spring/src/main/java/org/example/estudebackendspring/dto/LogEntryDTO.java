package org.example.estudebackendspring.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Data
@Setter
@Getter
public class LogEntryDTO {
    private Long logId;
    private String entity;
    private Long entityId;
    private LocalDateTime timestamp;
    private String content;
    private String actionType;
    private Long relatedEntityId;
    private String relatedEntity;
    private UserDTO user;

    // Constructor
    public LogEntryDTO(Long logId, String entity, Long entityId, LocalDateTime timestamp,
                       String content, String actionType, Long relatedEntityId,String relatedEntity, UserDTO user) {
        this.logId = logId;
        this.entity = entity;
        this.entityId = entityId;
        this.timestamp = timestamp;
        this.content = content;
        this.actionType = actionType;
        this.relatedEntityId = relatedEntityId;
        this.relatedEntity = relatedEntity;
        this.user = user;
    }

    // Getters & Setters
}
