package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.estudebackendspring.enums.AttendanceStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceStudentDTO {
    private Long studentId;
    private String studentCode;
    private String fullName;
    private Boolean attended; // true = đã điểm danh, false = chưa
    private AttendanceStatus status; // nullable
    private Long attendanceId; // nullable
    private LocalDateTime timestamp; // nullable (nếu cần)
}

