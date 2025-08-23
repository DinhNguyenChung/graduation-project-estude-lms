package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.enums.ActionType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "log_entries")
public class LogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;
      private String action;
    private String entity;
    private Long entityId;
    private LocalDateTime timestamp;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Enumerated(EnumType.STRING)
    private ActionType actionType;
    private Long relatedEntityId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
