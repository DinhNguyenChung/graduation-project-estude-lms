package org.example.estudebackendspring.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for Question Bank Statistics Overview
 * Used in Admin Analytics Dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionBankStatisticsDTO {
    
    /**
     * Total number of questions in bank
     */
    private Integer totalQuestions;
    
    /**
     * Number of active questions (can be used in assessments)
     */
    private Integer activeQuestions;
    
    /**
     * Number of inactive questions
     */
    private Integer inactiveQuestions;
    
    /**
     * Count by difficulty level
     * Key: "EASY", "MEDIUM", "HARD", "EXPERT"
     * Value: count
     */
    private Map<String, Integer> byDifficulty;
    
    /**
     * Count by subject
     * Key: subject name
     * Value: count
     */
    private Map<String, Integer> bySubject;
    
    /**
     * Count by topic
     * Key: topic name
     * Value: count
     */
    private Map<String, Integer> byTopic;
}
