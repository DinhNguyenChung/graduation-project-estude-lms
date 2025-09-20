package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.estudebackendspring.enums.GradeLevel;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class ClassDTO {
    private Long classId;
    private String name;
    private GradeLevel gradeLevel;
    private List<TermInfo> terms;
    private Integer classSize;
    private Long teacherId;
    private String homeroomTeacherName;

    @Data
    @AllArgsConstructor
    public static class TermInfo {
        private Long termId;
        private String name;
        private Date beginDate;
        private Date endDate;
    }
}