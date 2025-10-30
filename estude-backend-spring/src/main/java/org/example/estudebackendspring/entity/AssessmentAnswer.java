package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity to store individual answer for each question in assessment
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assessment_answers")
public class AssessmentAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;
    
    /**
     * Parent submission
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    @JsonIgnore
    private AssessmentSubmission assessmentSubmission;
    
    /**
     * Question that was answered
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnore
    private Question question;
    
    /**
     * Topic of the question (denormalized for easier querying)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    @JsonIgnore
    private Topic topic;
    
    /**
     * Student's chosen option
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chosen_option_id")
    @JsonIgnore
    private QuestionOption chosenOption;
    
    /**
     * Whether the answer is correct
     */
    @Column(nullable = false)
    private Boolean isCorrect;
    
    /**
     * Difficulty level of this question
     */
    @Column(length = 20)
    private String difficultyLevel;
}
