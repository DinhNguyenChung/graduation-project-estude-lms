package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.estudebackendspring.enums.AssignmentType;

import java.time.LocalDateTime;

/**
 * DTO for Assignment response to avoid lazy loading issues
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentResponseDTO {
    private Long assignmentId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Integer timeLimit;
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
    private Boolean isExam;
    private LocalDateTime startDate;
    
    // Teacher info
    private Long teacherId;
    private String teacherName;
    
    // ClassSubject info
    private Long classSubjectId;
    private String subjectName;
    private String className;
}
