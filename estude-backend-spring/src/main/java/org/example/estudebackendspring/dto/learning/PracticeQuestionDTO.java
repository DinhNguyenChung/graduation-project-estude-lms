package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeQuestionDTO {
    private String topic;
    private String subtopic;
    private String question;
    private List<String> options;
    
    @JsonProperty("correct_answer")
    private Integer correctAnswer;
    
    private String explanation;
    
    @JsonProperty("difficulty_level")
    private String difficultyLevel;
    
    @JsonProperty("study_hint")
    private String studyHint;
}
