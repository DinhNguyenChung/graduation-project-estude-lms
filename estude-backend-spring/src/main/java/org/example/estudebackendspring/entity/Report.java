package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.enums.ReportType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;
    
    private LocalDateTime generatedAt;
    
    @Enumerated(EnumType.STRING)
    private ReportType reportType;
      @Column(columnDefinition = "TEXT")
    private String content; // JSON
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
