package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "strategies")
public class Strategy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long strategyId;
    
    private Float targetMidTerm;
    private Float requiredFinal;
    private String description;
    private Boolean feasible;
}
