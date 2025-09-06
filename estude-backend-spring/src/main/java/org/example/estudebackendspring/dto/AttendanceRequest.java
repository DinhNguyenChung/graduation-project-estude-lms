package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.estudebackendspring.enums.AttendanceMethod;
import org.example.estudebackendspring.enums.AttendanceStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRequest {
    private Long classSubjectId;
    private Long studentId;   // nếu teacher điểm danh giúp hoặc student tự điểm danh
    private Long teacherId;   // optional: id của giáo viên nếu giáo viên điểm danh
    private AttendanceMethod method;
    private AttendanceStatus status;
    private Double gpsLatitude;
    private Double gpsLongitude;
}
