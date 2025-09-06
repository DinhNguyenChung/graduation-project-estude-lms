package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @JsonBackReference
    private AttendanceSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Enumerated(EnumType.STRING)
    private AttendanceMethod method;

    private Double gpsLatitude; // Vị trí của học sinh khi điểm danh
    private Double gpsLongitude;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    private LocalDateTime timestamp;
}
