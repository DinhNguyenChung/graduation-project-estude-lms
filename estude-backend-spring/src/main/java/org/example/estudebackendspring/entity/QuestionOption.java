package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question_options")
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;
    
    private String optionText;
    private Boolean isCorrect;
    @Column(name = "option_order")
    private Integer optionOrder;


    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;
    
    @OneToMany(mappedBy = "chosenOption", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Answer> answers;
}
