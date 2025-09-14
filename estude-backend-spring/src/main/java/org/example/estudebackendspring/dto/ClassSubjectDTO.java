package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassSubjectDTO {
    private Long classSubjectId;
    private String subjectName;
    private String teacherName;
    private Long termId;
    private String termName;
    private Long classId;
    private String className;

}
