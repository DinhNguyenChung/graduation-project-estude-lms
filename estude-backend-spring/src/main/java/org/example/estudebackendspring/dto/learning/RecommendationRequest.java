package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    @JsonProperty("submission_id")
    private String submissionId;
    
    @JsonProperty("feedback_data")
    private FeedbackDataDTO feedbackData;
}
