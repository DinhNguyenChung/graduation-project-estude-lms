package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Summary DTO for listing student's assessment submissions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentSubmissionSummaryDTO {
    
    private Long submissionId;
    private String assessmentId;
    private Long subjectId;
    private String subjectName;
    private String gradeLevel;
    private String difficulty;
    private Boolean improvementEvaluated;
    
    // Results
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Float score;
    private String performanceLevel;
    
    // Timing
    private LocalDateTime submittedAt;
    private Integer timeTaken;
}
