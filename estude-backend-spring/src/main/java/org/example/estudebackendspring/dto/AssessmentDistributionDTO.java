package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for question distribution statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDistributionDTO {
    private Map<String, Integer> byTopic;      // topicId -> count
    private Map<String, Integer> byDifficulty; // "EASY"/"MEDIUM"/"HARD" -> count
}
