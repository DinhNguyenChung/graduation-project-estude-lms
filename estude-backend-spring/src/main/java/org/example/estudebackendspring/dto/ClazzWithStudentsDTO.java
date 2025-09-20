package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.estudebackendspring.entity.Term;

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
    private List<Term> terms;
    private List<StudentDTO> students;
}

