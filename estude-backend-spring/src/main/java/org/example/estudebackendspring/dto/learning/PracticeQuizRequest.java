package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeQuizRequest {
    private String subject;
    private List<String> topics;
    
    @JsonProperty("num_questions")
    private Integer numQuestions;
    
    private String difficulty; // "easy", "medium", "hard", "mixed"
}
