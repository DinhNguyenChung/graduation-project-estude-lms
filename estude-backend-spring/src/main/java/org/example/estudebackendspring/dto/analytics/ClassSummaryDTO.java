package org.example.estudebackendspring.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary information for a single class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassSummaryDTO {
    
    @JsonProperty("class_id")
    private Long classId;
    
    @JsonProperty("class_name")
    private String className;
    
    @JsonProperty("grade_level")
    private String gradeLevel;
    
    @JsonProperty("student_count")
    private Integer studentCount;
    
    @JsonProperty("avg_score")
    private Double avgScore;
    
    @JsonProperty("pass_rate")
    private Double passRate;
    
    @JsonProperty("excellent_rate")
    private Double excellentRate;
    
    /**
     * Performance trend: "IMPROVING", "STABLE", "DECLINING"
     */
    private String trend;
}
