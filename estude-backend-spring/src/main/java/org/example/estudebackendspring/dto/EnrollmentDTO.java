package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * DTO for Enrollment to avoid lazy loading issues
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDTO {
    private Long enrollmentId;
    private Date dateJoined;
    
    // Student info
    private Long studentId;
    private String studentCode;
    private String studentName;
    private String studentEmail;
    
    // Class info
    private Long classId;
    private String className;
    private String gradeLevel;
}
