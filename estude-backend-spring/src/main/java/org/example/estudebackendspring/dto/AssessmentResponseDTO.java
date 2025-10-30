package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Complete response DTO for assessment generation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResponseDTO {
    private String assessmentId;
    private Long subjectId;
    private String subjectName;
    private Integer totalQuestions;
    private String difficulty;
    private List<AssessmentQuestionDTO> questions;
    private AssessmentDistributionDTO distribution;
    private LocalDateTime createdAt;
}
