package org.example.estudebackendspring.dto;

import lombok.*;
import org.example.estudebackendspring.enums.DifficultyLevel;
import org.example.estudebackendspring.enums.QuestionType;

import java.util.List;

/**
 * DTO tóm tắt cho Question Bank (dùng cho danh sách)
 * Chỉ chứa thông tin cơ bản, không load relationships
 * Tối ưu cho performance khi list nhiều records
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionBankSummaryDTO {
    private Long questionId;
    private String questionText;
    private Float points;
    private QuestionType questionType;
    private DifficultyLevel difficultyLevel;
    private String topicName;
    private String subjectName;
    private Integer optionCount;
    private List<QuestionOptionDTO> questionOptions;
    
    /**
     * Constructor để sử dụng trong JPQL projection query
     */
    public QuestionBankSummaryDTO(
            Long questionId,
            String questionText,
            Float points,
            QuestionType questionType,
            DifficultyLevel difficultyLevel,
            String topicName,
            String subjectName,
            Long optionCount) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.points = points;
        this.questionType = questionType;
        this.difficultyLevel = difficultyLevel;
        this.topicName = topicName;
        this.subjectName = subjectName;
        this.optionCount = optionCount != null ? optionCount.intValue() : 0;
    }
}
