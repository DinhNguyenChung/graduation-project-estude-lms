package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentGradeResponse {
    private Long studentId;
    private String studentCode;
    private String fullName;

    private List<Float> regularScores;
    private Float midtermScore;
    private Float finalScore;
    private Float actualAverage;
    private Float predictedMidTerm;
    private Float predictedFinal;
    private Float predictedAverage;
    private String comment;
}

