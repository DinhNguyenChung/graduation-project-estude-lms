package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.estudebackendspring.enums.AttendanceMethod;
import org.example.estudebackendspring.enums.AttendanceStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRecordDTO {
    private Long attendanceId;
    private Long sessionId;
    private Long studentId;
    private String studentCode;
    private AttendanceMethod method;
    private Double gpsLatitude;
    private Double gpsLongitude;
    private AttendanceStatus status;
    private LocalDateTime timestamp;

}

