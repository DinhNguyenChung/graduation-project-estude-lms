package org.example.estudebackendspring.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for submitting assessment answers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAssessmentRequest {
    
    @NotNull(message = "Assessment ID is required")
    private String assessmentId;
    
    @NotNull(message = "Student ID is required")
    private Long studentId;
    
    @NotNull(message = "Subject ID is required")
    private Long subjectId;
    
    @NotNull(message = "Grade level is required")
    private String gradeLevel;
    
    @NotNull(message = "Difficulty is required")
    private String difficulty;
    
    @NotEmpty(message = "Answers cannot be empty")
    private List<AssessmentAnswerRequest> answers;
    
    /**
     * Time taken in seconds (optional)
     */
    private Integer timeTaken;
    
    /**
     * Individual answer for one question
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentAnswerRequest {
        @NotNull(message = "Question ID is required")
        private Long questionId;
        
        @NotNull(message = "Chosen option ID is required")
        private Long chosenOptionId;
    }
}
