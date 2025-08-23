package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.enums.TargetType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "targets")
public class Target {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long targetId;
    
    @Enumerated(EnumType.STRING)
    private TargetType targetType;
    
    private Float requiredMidTerm;
    private Float requiredFinal;
    
    @ElementCollection
    private List<String> missingColumns = new ArrayList<>();
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_even_id")
    private Strategy strategyEven;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_optimal_id")
    private Strategy strategyOptimal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_grade_id")
    private SubjectGrade subjectGrade;
}
