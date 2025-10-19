package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionResultDTO {
    private Long submissionId;
    private int correctCount;
    private int totalQuestions;
    private float score;
//    private String aiFeedback;
}
