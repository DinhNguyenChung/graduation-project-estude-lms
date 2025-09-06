package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "attendance_sessions")
public class AttendanceSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    @JsonBackReference
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_subject_id")
    private ClassSubject classSubject;

    private String sessionName;
    private LocalDateTime createAt;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Double gpsLatitude; // Vị trí dự kiến của buổi điểm danh
    private Double gpsLongitude;

    @OneToMany(mappedBy = "session", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<AttendanceRecord> attendanceRecords;
}