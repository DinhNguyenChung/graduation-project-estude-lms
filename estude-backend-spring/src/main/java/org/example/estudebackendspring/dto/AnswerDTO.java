package org.example.estudebackendspring.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.estudebackendspring.enums.AnswerType;

@Data
@Setter
@Getter
public class AnswerDTO {
    private Long answerId;
    private String studentAnswerText;
    private Boolean isCorrect;
    private AnswerType answerType;
    private String fileUrl;
    private Float score;
    private String feedback;
    private QuestionAswerDTO question; // Thay vì questionId
    private QuestionOptionAnswerDTO chosenOption; // Thay vì chosenOptionId
}
