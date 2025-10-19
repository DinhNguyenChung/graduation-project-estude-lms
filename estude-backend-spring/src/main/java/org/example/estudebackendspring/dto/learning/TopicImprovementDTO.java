package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicImprovementDTO {
    private String topic;
    
    @JsonProperty("previous_accuracy")
    private Double previousAccuracy;
    
    @JsonProperty("new_accuracy")
    private Double newAccuracy;
    
    private Double improvement;
    
    @JsonProperty("improvement_percentage")
    private String improvementPercentage;
    
    private String status;
}
