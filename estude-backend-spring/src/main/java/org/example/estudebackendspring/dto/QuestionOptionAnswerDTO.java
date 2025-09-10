package org.example.estudebackendspring.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class QuestionOptionAnswerDTO {
    private Long optionId;
    private String optionText;
    private Boolean isCorrect;
    private Integer optionOrder;
    private String explanation;
    private Long questionId;
}
