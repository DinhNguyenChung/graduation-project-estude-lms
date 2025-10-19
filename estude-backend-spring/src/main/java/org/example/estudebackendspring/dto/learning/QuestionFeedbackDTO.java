package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionFeedbackDTO {
    private String question;
    
    @JsonProperty("student_answer")
    private String studentAnswer;
    
    @JsonProperty("correct_answer")
    private String correctAnswer;
    
    @JsonProperty("is_correct")
    private Boolean isCorrect;
    
    private String explanation;
    private String topic;
    private String subtopic;
    
    @JsonProperty("difficulty_level")
    private String difficultyLevel;
}
