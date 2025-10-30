package org.example.estudebackendspring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for topic-wise statistics for a student
 * Shows accuracy percentage for each topic across all submissions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicStatisticsDTO {
    
    /**
     * Topic name
     */
    private String topic;
    
    /**
     * Total questions answered for this topic
     */
    @JsonProperty("total_questions")
    private Long totalQuestions;
    
    /**
     * Correct answers for this topic
     */
    @JsonProperty("correct_answers")
    private Long correctAnswers;
    
    /**
     * Accuracy percentage (0.0 - 1.0)
     * Example: 0.6 means 60%
     */
    private Double accuracy;
    
    /**
     * Number of assessments containing this topic
     */
    @JsonProperty("assessment_count")
    private Long assessmentCount;
}
