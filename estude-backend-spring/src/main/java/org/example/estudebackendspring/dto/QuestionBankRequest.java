package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionBankRequest {
    private String questionText;
    private Float points;
    private String questionType; // "MULTIPLE_CHOICE", "TRUE_FALSE", etc.
    private Long topicId;
    private String difficultyLevel; // "EASY", "MEDIUM", "HARD"
    private String attachmentUrl;
    private java.util.List<QuestionOptionRequest> options;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionOptionRequest {
        private String optionText;
        private Boolean isCorrect;
        private Integer optionOrder;
    }
}
