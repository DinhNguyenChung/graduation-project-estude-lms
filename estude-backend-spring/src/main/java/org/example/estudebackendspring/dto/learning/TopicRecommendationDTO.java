package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicRecommendationDTO {
    @JsonProperty("study_focus")
    private String studyFocus;
    
    @JsonProperty("practice_suggestion")
    private String practiceSuggestion;
    
    @JsonProperty("resource_hint")
    private String resourceHint;
    
    @JsonProperty("study_method")
    private String studyMethod;
    
    @JsonProperty("common_pitfalls")
    private String commonPitfalls;
}
