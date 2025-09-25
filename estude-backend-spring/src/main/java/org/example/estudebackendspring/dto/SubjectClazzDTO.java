package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectClazzDTO {
    private Long subjectId;
    private String name;
    private String description;
    private List<SchoolDTO> schools;
}
