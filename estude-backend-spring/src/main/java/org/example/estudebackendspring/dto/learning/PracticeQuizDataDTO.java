package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeQuizDataDTO {
    private String subject;
    private List<String> topics;
    private String difficulty;
    
    @JsonProperty("total_questions")
    private Integer totalQuestions;
    
    @JsonProperty("generated_at")
    private LocalDateTime generatedAt;
    
    private List<PracticeQuestionDTO> questions;
}
