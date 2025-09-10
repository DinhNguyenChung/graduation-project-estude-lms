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
    private String teacherName;
    private Long classSubjectId;
    private Long subjectId;
    private String subjectName;
    private String sessionName;
    private Long classId;
    private String className;
    private LocalDateTime createAt;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double gpsLatitude;
    private Double gpsLongitude;
    private AttendanceStatus status;

}
