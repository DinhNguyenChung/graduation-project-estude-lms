package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.enums.SubmissionStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "submissions")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long submissionId;

    private LocalDateTime submittedAt;
    private String fileUrl;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    private Boolean isLate;
    private Integer attemptNumber;
    private LocalDateTime autoGradedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    @JsonIgnore
    private Assignment assignment;

    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Grade grade;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Answer> answers;
}