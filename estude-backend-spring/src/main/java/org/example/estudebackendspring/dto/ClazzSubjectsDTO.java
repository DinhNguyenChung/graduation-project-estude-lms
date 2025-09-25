package org.example.estudebackendspring.dto;

import lombok.*;
import org.example.estudebackendspring.enums.GradeLevel;

import java.util.Date;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClazzSubjectsDTO {
    private Long classSubjectId;
    private TermDTO term;
    private SubjectClazzDTO subject;
    private TeacherDTO teacher;
    private Long classId;
    private String className;
    private GradeLevel gradeLevel;


}
