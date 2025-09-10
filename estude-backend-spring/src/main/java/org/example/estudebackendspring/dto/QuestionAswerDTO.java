package org.example.estudebackendspring.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.estudebackendspring.enums.QuestionType;

import java.util.List;
@Data
@Setter
@Getter
public class QuestionAswerDTO {
    private Long questionId;
    private String questionText;
    private Float points;
    private QuestionType questionType;
    private Integer questionOrder;
    private String attachmentUrl;
    private Long assignmentId;
    private List<QuestionOptionAnswerDTO> options;
}
