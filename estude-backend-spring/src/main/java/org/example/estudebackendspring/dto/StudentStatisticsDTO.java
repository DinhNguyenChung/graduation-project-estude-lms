package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentStatisticsDTO {
    private Long studentId;
    private String fullName;
    private Double averageScore; // actualAverage
    private Integer rank;        // thứ hạng
    private Integer totalStudents; // tổng số học sinh lớp
    private Integer totalSubjects; // số môn hiện tại
    private Integer completedSubjects; // số môn đã hoàn thành
    private Double submissionRate;
    private Double lateRate;
    private Double attendanceRate;
    private Long totalSessions;
    private Long absentSessions;
}

