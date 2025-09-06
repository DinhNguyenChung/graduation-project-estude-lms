package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentForAttendanceDTO {
    private Long studentId;
    private String studentName;
    private String studentCode;
}
