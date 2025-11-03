package org.example.estudebackendspring.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Detailed student performance across topics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPerformanceDTO {
    
    @JsonProperty("student_id")
    private Long studentId;
    
    @JsonProperty("student_name")
    private String studentName;
    
    @JsonProperty("student_code")
    private String studentCode;
    
    @JsonProperty("overall_score")
    private Double overallScore;
    
    @JsonProperty("topic_scores")
    private List<TopicScoreDTO> topicScores;
    
    @JsonProperty("weak_topics")
    private List<WeakTopicDTO> weakTopics; // Topics where score < 5.0
    
    @JsonProperty("strong_topics")
    private List<String> strongTopics; // Topics where score >= 9.0
    
    @JsonProperty("progress_trend")
    private String progressTrend; // "IMPROVING", "STABLE", "DECLINING"
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicScoreDTO {
        @JsonProperty("topic_name")
        private String topicName;
        
        private Double score;
        
        @JsonProperty("completed_assignments")
        private Integer completedAssignments;
        
        @JsonProperty("total_assignments")
        private Integer totalAssignments;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeakTopicDTO {
        @JsonProperty("topic_name")
        private String topicName;
        
        private Double score;
        
        @JsonProperty("recommended_resources")
        private List<String> recommendedResources;
    }
}
