package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImprovementDataDTO {
    @JsonProperty("student_id")
    private Long studentId;
    
    private String subject;
    
    @JsonProperty("previous_result_id")
    private String previousResultId;  // ID của AIAnalysisResult từ Layer 1
    
    @JsonProperty("result_id")
    private String resultId;  // ID của AIAnalysisResult từ Layer 3.5
    
    @JsonProperty("evaluation_time")
    private LocalDateTime evaluationTime;
    
    private String summary;
    private List<TopicImprovementDTO> topics;
    
    @JsonProperty("next_action")
    private String nextAction;
    
    @JsonProperty("overall_improvement")
    private OverallImprovementDTO overallImprovement;
}
