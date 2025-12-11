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
public class ClazzWithStudentsDTO {
    private Long classId;
    private String name;
    private String gradeLevel;
    private Integer classSize;
    private List<TermDTO> terms;
    private List<StudentDTO> students;
}

