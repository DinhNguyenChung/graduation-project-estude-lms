package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "teachers")
public class Teacher extends User {
    @Column(unique = true, nullable = false)
    private String teacherCode;

    @Temporal(TemporalType.DATE)
    private Date hireDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    private boolean isAdmin;
    private boolean isHomeroomTeacher;
    
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClassSubject> classSubjects;
      @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Grade> grades;
    
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Assignment> assignments;
}
