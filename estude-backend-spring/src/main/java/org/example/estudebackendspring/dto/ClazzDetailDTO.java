package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.estudebackendspring.enums.GradeLevel;

import java.util.List;

/**
 * Detailed DTO for Clazz entity including all related information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClazzDetailDTO {
    private Long classId;
    private String name;
    private GradeLevel gradeLevel;
    private Integer classSize;
    
    // Homeroom Teacher Info
    private Long homeroomTeacherId;
    private String homeroomTeacherName;
    private String homeroomTeacherCode;
    
    // School Info
    private Long schoolId;
    private String schoolName;
    
    // Terms
    private List<TermDTO> terms;
}
