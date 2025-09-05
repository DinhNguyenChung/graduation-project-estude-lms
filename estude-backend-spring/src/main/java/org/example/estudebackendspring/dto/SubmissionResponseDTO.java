package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionResponseDTO {
    // Submission info
    private Long submissionId;
    private LocalDateTime submittedAt;
    private String fileUrl;
    private String content;
    private String status;
    private Boolean isLate;
    private Integer attemptNumber;
    private LocalDateTime autoGradedAt;

    // Student info
    private Long studentId;
    private String studentCode;
    private String studentName;

    // Assignment info
    private Long assignmentId;
    private String assignmentTitle;
    private LocalDateTime dueDate;

    // Grade info
    private Long gradeId;
    private Float score;
    private String gradeComment;
}

