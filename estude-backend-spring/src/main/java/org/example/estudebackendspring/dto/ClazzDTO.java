package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.estudebackendspring.enums.GradeLevel;

/**
 * DTO for Clazz entity to avoid lazy loading issues
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClazzDTO {
    private Long classId;
    private String name;
    private GradeLevel gradeLevel;
    private Integer classSize;
    private Long homeroomTeacherId;
    private String homeroomTeacherName;
    private Long schoolId;
    private String schoolName;
}
