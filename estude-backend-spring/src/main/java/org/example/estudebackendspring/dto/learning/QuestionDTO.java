package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private String question;
    private List<String> options;
    
    @JsonProperty("correct_answer")
    private Integer correctAnswer;
    
    @JsonProperty("student_answer")
    private Integer studentAnswer;
}
