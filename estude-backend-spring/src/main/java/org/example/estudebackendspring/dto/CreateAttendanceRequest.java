package org.example.estudebackendspring.dto;

import lombok.Data;
import org.example.estudebackendspring.enums.AttendanceMethod;

import java.time.LocalDateTime;

@Data
public class CreateAttendanceRequest {
    private Long teacherId;
    private Long classSubjectId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private AttendanceMethod method;
    private Double gpsLatitude;
    private Double gpsLongitude;
}
