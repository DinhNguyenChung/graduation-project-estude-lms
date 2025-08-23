package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.enums.AssignmentType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assignments")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private LocalDateTime dueDate;
    private Integer timeLimit;
    
    @Enumerated(EnumType.STRING)
    private AssignmentType type;
    
    private String attachmentUrl;
    private Float maxScore;
    private Boolean isPublished;
    private Boolean allowLateSubmission;
    private Float latePenalty;
    private Integer submissionLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String answerKeyFileUrl;
    private Boolean isAutoGraded;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_subject_id")
    private ClassSubject classSubject;
    
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Question> questions;
    
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Submission> submissions;
}
