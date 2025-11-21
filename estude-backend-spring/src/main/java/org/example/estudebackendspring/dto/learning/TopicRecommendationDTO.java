package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicRecommendationDTO {
    @JsonProperty("study_focus")
    @JsonDeserialize(using = FlexibleStringDeserializer.class)
    private String studyFocus;
    
    @JsonProperty("practice_suggestion")
    @JsonDeserialize(using = FlexibleStringDeserializer.class)
    private String practiceSuggestion;
    
    @JsonProperty("resource_hint")
    @JsonDeserialize(using = FlexibleStringDeserializer.class)
    private String resourceHint;
    
    @JsonProperty("study_method")
    @JsonDeserialize(using = FlexibleStringDeserializer.class)
    private String studyMethod;
    
    @JsonProperty("common_pitfalls")
    @JsonDeserialize(using = FlexibleStringDeserializer.class)
    private String commonPitfalls;
}
