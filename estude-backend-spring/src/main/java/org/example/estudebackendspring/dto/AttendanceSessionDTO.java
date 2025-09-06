package org.example.estudebackendspring.dto;

import lombok.*;
import org.example.estudebackendspring.enums.AttendanceStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AttendanceSessionDTO {
    private Long sessionId;
    private Long teacherId;
    private String teacherCode;
    private Long classSubjectId;
    private String sessionName;
    private LocalDateTime createAt;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double gpsLatitude;
    private Double gpsLongitude;
    private AttendanceStatus status;

}
