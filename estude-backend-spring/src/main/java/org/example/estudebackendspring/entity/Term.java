package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "terms")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Term {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long termId;


    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private Date beginDate; // Ví dụ: Kỳ 1: 2025-08-01

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull(message = "Ngày kết thúc không được để trống")
    private Date endDate; // Ví dụ: Kỳ 1: 2025-12-31

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    @JsonIgnore
    private Clazz clazz; // Liên kết với lớp

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ClassSubject> classSubjects; // Môn học theo kỳ

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Schedule> schedules; // Lịch học theo kỳ
}
