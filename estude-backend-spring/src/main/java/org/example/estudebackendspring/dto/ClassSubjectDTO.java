package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassSubjectDTO {
    private Long classSubjectId;
    private String subjectName;
    private String teacherName;
    private String className;

}
