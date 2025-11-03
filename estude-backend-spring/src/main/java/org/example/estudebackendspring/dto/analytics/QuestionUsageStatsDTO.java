package org.example.estudebackendspring.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Question Usage Statistics
 * Shows how a question is being used and student performance on it
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionUsageStatsDTO {
    
    @JsonProperty("question_id")
    private Long questionId;
    
    @JsonProperty("question_text")
    private String questionText;
    
    private String topic;
    
    private String difficulty;
    
    @JsonProperty("usage_stats")
    private UsageStats usageStats;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageStats {
        /**
         * Number of times this question has been used in assessments
         */
        @JsonProperty("times_used")
        private Integer timesUsed;
        
        /**
         * Total number of student attempts
         */
        @JsonProperty("total_attempts")
        private Integer totalAttempts;
        
        /**
         * Number of correct answers
         */
        @JsonProperty("correct_attempts")
        private Integer correctAttempts;
        
        /**
         * Number of incorrect answers
         */
        @JsonProperty("incorrect_attempts")
        private Integer incorrectAttempts;
        
        /**
         * Average accuracy percentage
         */
        @JsonProperty("average_accuracy")
        private Double averageAccuracy;
        
        /**
         * Average time spent on this question (seconds)
         */
        @JsonProperty("average_time_seconds")
        private Double averageTimeSeconds;
    }
    
    @JsonProperty("common_mistakes")
    private List<CommonMistakeDTO> commonMistakes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommonMistakeDTO {
        @JsonProperty("incorrect_answer")
        private String incorrectAnswer;
        
        private Integer count;
        
        private Double percentage;
        
        private String reason;
    }
}
