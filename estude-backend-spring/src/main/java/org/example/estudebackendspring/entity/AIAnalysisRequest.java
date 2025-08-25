package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.Converter.JsonNodeConverter;
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
    private LocalDateTime requestDate;
    @Enumerated(EnumType.STRING)
    private AnalysisType analysisType;
    @Column(columnDefinition = "TEXT")
    @Convert(converter = JsonNodeConverter.class)
    private JsonNode dataPayload; // JSON string
    
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL)
    @JsonIgnore
    private AIAnalysisResult result;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @JsonIgnore
    private Student student;
}
