package org.example.estudebackendspring.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Homeroom Teacher - Complete class overview
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeroomClassDTO {
    
    @JsonProperty("class_id")
    private Long classId;
    
    @JsonProperty("class_name")
    private String className;
    
    @JsonProperty("grade_level")
    private String gradeLevel;
    
    @JsonProperty("homeroom_teacher")
    private String homeroomTeacher;
    
    @JsonProperty("student_count")
    private Integer studentCount;
    
    @JsonProperty("overall_performance")
    private OverallPerformanceDTO overallPerformance;
    
    @JsonProperty("subject_performance")
    private List<SubjectPerformanceDTO> subjectPerformance;
    
    @JsonProperty("top_performers")
    private List<StudentRankDTO> topPerformers; // Top 5 students
    
    @JsonProperty("at_risk_students")
    private List<StudentRankDTO> atRiskStudents; // Students with avg < 5.0
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectPerformanceDTO {
        @JsonProperty("subject_name")
        private String subjectName;
        
        @JsonProperty("term_name")
        private String termName;
        
        @JsonProperty("teacher_name")
        private String teacherName;
        
        @JsonProperty("avg_score")
        private Double avgScore;
        
        @JsonProperty("pass_rate")
        private Double passRate;
        
        @JsonProperty("excellent_rate")
        private Double excellentRate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentRankDTO {
        @JsonProperty("student_id")
        private Long studentId;
        
        @JsonProperty("student_name")
        private String studentName;
        
        @JsonProperty("student_code")
        private String studentCode;
        
        @JsonProperty("overall_score")
        private Double overallScore;
        
        private Integer rank;
    }
}
