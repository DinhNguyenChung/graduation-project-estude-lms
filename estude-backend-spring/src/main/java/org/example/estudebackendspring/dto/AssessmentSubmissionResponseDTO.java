package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO after submitting assessment
 * Includes scoring, correct answers, and breakdown by topic/difficulty
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentSubmissionResponseDTO {
    
    private Long submissionId;
    private String assessmentId;
    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectName;
    private String gradeLevel;
    private String difficulty;
    
    // Scoring
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Float score;  // Percentage (0-100)
    private String performanceLevel;  // "EXCELLENT", "GOOD", "AVERAGE", "NEEDS_IMPROVEMENT"
    
    // Timing
    private LocalDateTime submittedAt;
    private Integer timeTaken;  // seconds
    
    // Detailed results
    private List<AssessmentAnswerResultDTO> answers;
    
    // Statistics breakdown
    private AssessmentStatisticsDTO statistics;
    
    /**
     * Individual answer result
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentAnswerResultDTO {
        private Long questionId;
        private String questionText;
        private Long topicId;
        private String topicName;
        private String difficultyLevel;
        private Long chosenOptionId;
        private String chosenOptionText;
        private Boolean isCorrect;
        private Long correctOptionId;
        private String correctOptionText;
        private String explanation;
    }
    
    /**
     * Statistics breakdown by topic and difficulty
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentStatisticsDTO {
        private Map<String, TopicStatDTO> byTopic;  // topicName -> stats
        private Map<String, DifficultyStatDTO> byDifficulty;  // "EASY"/"MEDIUM"/"HARD" -> stats
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TopicStatDTO {
            private Long topicId;
            private String topicName;
            private Integer totalQuestions;
            private Integer correctAnswers;
            private Float accuracy;  // Percentage (0-100)
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DifficultyStatDTO {
            private String difficulty;
            private Integer totalQuestions;
            private Integer correctAnswers;
            private Float accuracy;  // Percentage (0-100)
        }
    }
}
