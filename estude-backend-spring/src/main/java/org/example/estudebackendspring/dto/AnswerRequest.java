package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerRequest {
    private Long questionId;
    private Long chosenOptionId; // for option questions
    private String textAnswer;   // for text answers
    private String fileUrl;      // if file uploaded separately and stored
}
