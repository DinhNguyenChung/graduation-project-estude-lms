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
public class RecommendationDataDTO {
    @JsonProperty("student_name")
    private String studentName;
    
    private String subject;
    private LocalDateTime timestamp;
    
    @JsonProperty("weak_topics")
    private List<WeakTopicDTO> weakTopics;
    
    @JsonProperty("overall_advice")
    private String overallAdvice;
    
    @JsonProperty("priority_order")
    private List<String> priorityOrder;
}
