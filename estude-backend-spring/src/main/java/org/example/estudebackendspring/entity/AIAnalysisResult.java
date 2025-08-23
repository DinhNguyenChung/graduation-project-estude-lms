package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_analysis_results")
public class AIAnalysisResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultId;
    private Float predictedAverage;
    private String predictedPerformance;
    private String actualPerformance;
    private String comment;
    
    @Column(columnDefinition = "TEXT")
    private String suggestedActions; // JSON string
    
    @Column(columnDefinition = "TEXT")
    private String detailedAnalysis; // JSON string
      @Column(columnDefinition = "TEXT")
    private String statistics; // JSON string
    
    private LocalDateTime generatedAt;
    
    // Store just the request ID to avoid potential circular references
    @Column(name = "request_id")
    private Long requestId;
    
    // This maintains the association but tells JPA not to use it for database operations
    @OneToOne
    @JoinColumn(name = "request_id", insertable = false, updatable = false)
    private AIAnalysisRequest request;
}
