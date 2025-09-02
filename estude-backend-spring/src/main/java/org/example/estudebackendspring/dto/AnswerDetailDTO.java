package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerDetailDTO {
    private Long answerId;
    private Long questionId;
    private String studentAnswerText;
    private Boolean isCorrect;
    private Float score;
    private String feedback;
}
