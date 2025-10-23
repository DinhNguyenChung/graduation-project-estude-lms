package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImprovementRequest {
    @JsonProperty("student_id")
    private Long studentId;
    
    private String subject;
    
    @JsonProperty("previous_results_id")
    private String previousResultsId;  // ID của AIAnalysisResult từ Layer 1 (Feedback gốc)
    
    @JsonProperty("result_id")
    private String resultId;  // ID của AIAnalysisResult từ Layer 3.5 (Practice Review)
    
    @JsonProperty("previous_results")
    private List<TopicResultDTO> previousResults;
    
    @JsonProperty("new_results")
    private List<TopicResultDTO> newResults;
}

