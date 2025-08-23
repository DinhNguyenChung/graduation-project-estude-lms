package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.enums.AnswerType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "answers")
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;
    
    private String studentAnswerText;
    private Boolean isCorrect;
    
    @Enumerated(EnumType.STRING)
    private AnswerType answerType;
    
    private String fileUrl;
    private Float score;
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private QuestionOption chosenOption;
}
