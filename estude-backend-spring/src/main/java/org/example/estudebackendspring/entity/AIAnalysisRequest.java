package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.enums.AnalysisType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_analysis_requests")
public class AIAnalysisRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;
    private LocalDateTime requestDate;    @Enumerated(EnumType.STRING)
    private AnalysisType analysisType;
    @Column(columnDefinition = "TEXT")
    private String dataPayload; // JSON string
    
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL)
    private AIAnalysisResult result;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;
}
