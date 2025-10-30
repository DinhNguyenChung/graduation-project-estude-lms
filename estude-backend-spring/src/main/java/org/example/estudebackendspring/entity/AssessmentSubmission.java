package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity to store student's assessment submission
 * Stores answers, scoring, and completion time
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assessment_submissions")
public class AssessmentSubmission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long submissionId;
    
    /**
     * Unique assessment ID from generation
     */
    @Column(nullable = false)
    private String assessmentId;
    
    /**
     * Student who submitted
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private Student student;
    
    /**
     * Subject of the assessment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    @JsonIgnore
    private Subject subject;
    
    /**
     * Grade level when taken
     */
    @Column(length = 20)
    private String gradeLevel;
    
    /**
     * Difficulty mode
     */
    @Column(length = 20)
    private String difficulty;
    
    /**
     * Total questions in assessment
     */
    @Column(nullable = false)
    private Integer totalQuestions;
    
    /**
     * Number of correct answers
     */
    @Column(nullable = false)
    private Integer correctAnswers;
    
    /**
     * Score percentage (0-100)
     */
    @Column(nullable = false)
    private Float score;
    
    /**
     * Time submitted
     */
    @Column(nullable = false)
    private LocalDateTime submittedAt;
    
    /**
     * Time taken to complete (in seconds)
     */
    private Integer timeTaken;
    
    /**
     * Flag to mark if this submission has been evaluated for improvement (Layer 4)
     * Default: false - not yet evaluated
     * Set to true after running improvement evaluation
     */
    @Column(name = "improvement_evaluated", nullable = false, columnDefinition = "boolean default false")
    private Boolean improvementEvaluated = false;


    /**
     * Individual answers for each question
     */
    @OneToMany(mappedBy = "assessmentSubmission", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<AssessmentAnswer> answers = new ArrayList<>();
}
