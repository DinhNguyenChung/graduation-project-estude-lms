package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho Layer 3.5: Review Practice Results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPracticeResponse {
    
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("data")
    private ReviewPracticeDataDTO data;
    
    @JsonProperty("message")
    private String message;
}
