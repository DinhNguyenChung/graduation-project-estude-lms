package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subject_grades")
public class SubjectGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subjectGradeId;
    
    @ElementCollection
    private List<Float> regularScores = new ArrayList<>();
    
    private Float midtermScore;
    private Float finalScore;
    private Float actualAverage;
    private Float predictedMidTerm;
    private Float predictedFinal;
    private Float predictedAverage;
    
    private String comment;
    
//    @OneToMany(mappedBy = "subjectGrade", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonIgnore
//    private List<Target> targets = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @JsonIgnore
    private Student student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_subject_id")
    @JsonIgnore
    private ClassSubject classSubject;
}
