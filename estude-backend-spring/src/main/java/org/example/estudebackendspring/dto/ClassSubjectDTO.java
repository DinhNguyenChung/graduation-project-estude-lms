package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.estudebackendspring.enums.GradeLevel;

import java.util.Date;

@Data
@AllArgsConstructor
public class ClassSubjectDTO {
    private Long classSubjectId;
    private String subjectName;
    private String teacherName;
    private Long termId;
    private String termName;
    private Date beginDate;
    private Date endDate;
    private Long classId;
    private String className;
    private GradeLevel gradeLevel;

}
