package org.example.estudebackendspring.dto;

import lombok.Data;
import org.example.estudebackendspring.enums.AttendanceStatus;

@Data
public class StudentAttendanceDTO {
    private Long studentId;
    private String studentCode;
    private String studentName; // Giả định entity User có trường name
    private AttendanceStatus status;
}
