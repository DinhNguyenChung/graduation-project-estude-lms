package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO cho Layer 3.5: Review Practice Results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPracticeRequest {
    @JsonProperty("assignment_id")
    private String assignmentId;
    @JsonProperty("student_name")
    private String studentName;
    
    @JsonProperty("subject")
    private String subject;
    
    @JsonProperty("questions")
    private List<PracticeQuestionReviewDTO> questions;
}
