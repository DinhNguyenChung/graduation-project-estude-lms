package org.example.estudebackendspring.dto;

import lombok.*;
import org.example.estudebackendspring.enums.DifficultyLevel;
import org.example.estudebackendspring.enums.QuestionType;

import java.util.List;

/**
 * DTO tối ưu cho Question Bank với đầy đủ thông tin
 * Tránh lazy loading và N+1 query problem
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionBankDTO {
    private Long questionId;
    private String questionText;
    private Float points;
    private QuestionType questionType;
    private DifficultyLevel difficultyLevel;
    private String attachmentUrl;
    private TopicInfo topic;
    private List<QuestionOptionDTO> options;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicInfo {
        private Long topicId;
        private String name;
        private SubjectInfo subject;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubjectInfo {
        private Long subjectId;
        private String name;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionOptionDTO {
        private Long optionId;
        private String optionText;
        private Boolean isCorrect;
        private Integer optionOrder;
    }
}
