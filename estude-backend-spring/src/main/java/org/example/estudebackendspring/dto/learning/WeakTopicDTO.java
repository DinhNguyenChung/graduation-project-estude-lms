package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeakTopicDTO {
    private String topic;
    
    @JsonProperty("error_count")
    private Integer errorCount;
    
    private Double percentage;
    private List<String> subtopics;
    private TopicRecommendationDTO recommendation;
}
