package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverallImprovementDTO {
    @JsonProperty("previous_average")
    private Double previousAverage;
    
    @JsonProperty("new_average")
    private Double newAverage;
    
    private Double improvement;
    
    @JsonProperty("improvement_percentage")
    private String improvementPercentage;
    
    private String direction;
}
