package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho phân tích accuracy theo topic trong Layer 3.5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicBreakdownDTO {
    
    @JsonProperty("topic")
    private String topic;
    
    @JsonProperty("correct")
    private Integer correct;
    
    @JsonProperty("total")
    private Integer total;
    
    @JsonProperty("accuracy")
    private Double accuracy;
}
