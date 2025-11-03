package org.example.estudebackendspring.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for Subject Teacher Overview
 * Shows all classes a teacher is teaching with overall stats
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherClassOverviewDTO {
    
    @JsonProperty("teacher_info")
    private TeacherInfo teacherInfo;
    
    @JsonProperty("overall_performance")
    private OverallPerformanceDTO overallPerformance;
    
    private List<ClassSummaryDTO> classes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherInfo {
        @JsonProperty("teacher_id")
        private Long teacherId;
        
        @JsonProperty("teacher_name")
        private String teacherName;
        
        private String subject;
        
        @JsonProperty("total_students")
        private Integer totalStudents;
    }
}
