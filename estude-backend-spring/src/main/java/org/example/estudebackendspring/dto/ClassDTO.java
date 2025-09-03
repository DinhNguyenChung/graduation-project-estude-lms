package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassDTO {
    private Long classId;
    private String name;
    private String term;
    private Integer classSize;
    private String homeroomTeacherName;
}