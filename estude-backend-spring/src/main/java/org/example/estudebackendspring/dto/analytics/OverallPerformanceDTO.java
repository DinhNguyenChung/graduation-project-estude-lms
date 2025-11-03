package org.example.estudebackendspring.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common DTO for overall performance statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverallPerformanceDTO {
    
    @JsonProperty("avg_score")
    private Double avgScore;
    
    @JsonProperty("pass_rate")
    private Double passRate; // Percentage of students with score >= 5.0
    
    @JsonProperty("excellent_rate")
    private Double excellentRate; // Percentage of students with score >= 9.0
    
    @JsonProperty("comparison_to_school")
    private ComparisonDTO comparisonToSchool;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonDTO {
        @JsonProperty("avg_score_diff")
        private Double avgScoreDiff; // +/- compared to school average
        
        @JsonProperty("pass_rate_diff")
        private Double passRateDiff;
        
        @JsonProperty("excellent_rate_diff")
        private Double excellentRateDiff;
    }
}
