package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionOptionDTO {
    private Long optionId;
    private String optionText;
    private Boolean isCorrect; // useful for teacher/admin; for student API you may hide
}
