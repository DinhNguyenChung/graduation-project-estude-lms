package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "classes")
public class Clazz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long classId;
    
    @Column(nullable = false)
    private String name;
    private String term;
    private Integer classSize;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homeroom_teacher_id", unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Teacher homeroomTeacher; // Mối quan hệ 1-1 với giáo viên chủ nhiệm
    @OneToMany(mappedBy = "clazz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Enrollment> enrollments;
    
    @OneToMany(mappedBy = "clazz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ClassSubject> classSubjects;
    
    @OneToMany(mappedBy = "clazz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Schedule> schedules;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private School school;


}
