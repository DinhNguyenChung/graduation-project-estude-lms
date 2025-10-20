package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho câu hỏi trong Layer 3.5 Review Practice Results
 * Dùng cho cả Request và Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeQuestionReviewDTO {
    
    @JsonProperty("question_id")
    private Integer questionId;
    
    @JsonProperty("question")
    private String question;
    
    @JsonProperty("options")
    private List<String> options;
    
    @JsonProperty("correct_answer")
    private Integer correctAnswer;
    
    @JsonProperty("student_answer")
    private Integer studentAnswer;
    
    // Response only fields (chỉ có trong Response)
    @JsonProperty("is_correct")
    private Boolean isCorrect;
    
    @JsonProperty("explanation")
    private String explanation;
    
    @JsonProperty("topic")
    private String topic;
    
    @JsonProperty("subtopic")
    private String subtopic;
    
    @JsonProperty("difficulty_level")
    private String difficultyLevel;
}
