package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "class_subjects")
/**
 * Junction entity that represents a subject taught in a specific class by a teacher.
 * This is a central entity that connects classes, subjects, teachers, and serves as
 * the context for assignments, schedules, and grades.
 */
public class ClassSubject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long classSubjectId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")

    private Clazz clazz;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    @JsonIgnore
    private Subject subject;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    @JsonIgnore
    private Teacher teacher;
    @OneToMany(mappedBy = "classSubject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Assignment> assignments;
    
    @OneToMany(mappedBy = "classSubject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Schedule> schedules;
    
    @OneToMany(mappedBy = "classSubject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SubjectGrade> subjectGrades;
    
    @OneToMany(mappedBy = "classSubject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AttendanceRecord> attendanceRecords;
}
