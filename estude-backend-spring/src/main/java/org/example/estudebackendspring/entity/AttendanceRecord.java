package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.enums.AttendanceMethod;
import org.example.estudebackendspring.enums.AttendanceStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;
    
    private LocalDateTime createAt;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @Enumerated(EnumType.STRING)
    private AttendanceMethod method;
    
    private Double gpsLatitude;
    private Double gpsLongitude;
    
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;
    
    private LocalDateTime timestamp;
//    private Long studentId;
    private Long teacherId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_subject_id")
    private ClassSubject classSubject;
}
