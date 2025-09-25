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
    private String sessionName; // Tên buổi điểm danh
    private Long studentId;
    private String studentName;
    private Long teacherId; // Mã giáo viên
    private String teacherName;
    private Long classId;
    private String className; // Tên lớp
    private Long classSubjectId;
    private Long subjectId;
    private String subjectName; // Tên môn học
    private AttendanceMethod method;
    private Double gpsLatitude;
    private Double gpsLongitude;
    private AttendanceStatus status;
    private LocalDateTime timestamp;
}

