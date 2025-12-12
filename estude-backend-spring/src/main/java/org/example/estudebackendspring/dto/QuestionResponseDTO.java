package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.estudebackendspring.enums.DifficultyLevel;
import org.example.estudebackendspring.enums.QuestionType;

import java.util.List;

/**
 * Full DTO for Question response to avoid lazy loading issues
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponseDTO {
    private Long questionId;
    private String questionText;
    private Float points;
    private QuestionType questionType;
    private Integer questionOrder;
    private String attachmentUrl;
    private DifficultyLevel difficultyLevel;
    private Boolean isQuestionBank;
    
    // Assignment info
    private Long assignmentId;
    private String assignmentTitle;
    
    // Topic info
    private Long topicId;
    private String topicName;
    
    // Options
    private List<QuestionOptionDTO> options;
}
