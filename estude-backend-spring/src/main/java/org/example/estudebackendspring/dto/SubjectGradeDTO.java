package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectGradeDTO {
    private Long subjectGradeId;
    private Long studentId;
    private Long classSubjectId;
    private List<Float> regularScores;
    private Float midtermScore;
    private Float finalScore;
    private Float actualAverage;
    private Float predictedMidTerm;
    private Float predictedFinal;
    private Float predictedAverage;
    private String comment;
}
