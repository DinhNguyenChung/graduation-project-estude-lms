package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho gợi ý bước tiếp theo trong Layer 3.5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NextStepsDTO {
    
    @JsonProperty("action")
    private String action;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("weak_topics")
    private List<String> weakTopics;
}
