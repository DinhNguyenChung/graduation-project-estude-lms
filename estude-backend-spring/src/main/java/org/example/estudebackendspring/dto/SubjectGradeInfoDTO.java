package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectGradeInfoDTO {
    // subjectGrade
    private Long subjectGradeId;
    private List<Float> regularScores;
    private Float midtermScore;
    private Float finalScore;
    private Float actualAverage;
    private String comment;
    private String rank;

    // subject
    private Long subjectId;
    private String subjectName;
    private String subjectDescription;

    // classSubject
    private Long classSubjectId;

    // teacher
    private Long teacherId;
    private String teacherName;

    // clazz
    private Long classId;
    private String className;
}
