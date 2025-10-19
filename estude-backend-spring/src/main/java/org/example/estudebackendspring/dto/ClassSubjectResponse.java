package org.example.estudebackendspring.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSubjectResponse {
    private Long classSubjectId;
    private Long subjectId;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private String termName;

    // Thông tin lớp
    private Long classId;
    private String className;
    private String gradeLevel;
}
