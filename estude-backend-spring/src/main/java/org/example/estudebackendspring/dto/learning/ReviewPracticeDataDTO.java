package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO chứa data chi tiết cho Layer 3.5 Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPracticeDataDTO {
    
    @JsonProperty("result_id")
    private Long resultId;  // ID của AIAnalysisResult - dùng cho Layer 4
    
    @JsonProperty("assignment_id")
    private String assignmentId;
    
    @JsonProperty("session_type")
    private String sessionType = "practice_review";
    
    @JsonProperty("student_name")
    private String studentName;
    
    @JsonProperty("subject")
    private String subject;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("summary")
    private FeedbackSummaryDTO summary;
    
    @JsonProperty("feedback")
    private List<PracticeQuestionReviewDTO> feedback;
    
    @JsonProperty("topic_breakdown")
    private List<TopicBreakdownDTO> topicBreakdown;
    
    @JsonProperty("next_steps")
    private NextStepsDTO nextSteps;
}
