package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for individual question in assessment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentQuestionDTO {
    private Long questionId;
    private String questionText;
    private String difficultyLevel;  // "EASY", "MEDIUM", "HARD"
    private Long topicId;
    private String topicName;
    private List<AssessmentOptionDTO> options;
    private String explanation;  // Can be null
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentOptionDTO {
        private Long optionId;
        private String optionText;
        private Boolean isCorrect;  // For backend use, will be removed in response
    }
}
