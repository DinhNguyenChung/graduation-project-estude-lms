package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAttendanceSessionRequest {
    private String sessionName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double gpsLatitude;
    private Double gpsLongitude;
}

