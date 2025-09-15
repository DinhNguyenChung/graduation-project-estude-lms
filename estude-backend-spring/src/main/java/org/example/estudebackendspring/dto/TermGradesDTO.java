package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TermGradesDTO {
    private Long termId;
    private String termName;
    private Date beginDate;
    private Date endDate;
    private List<SubjectGradeInfoDTO> subjects;
}
