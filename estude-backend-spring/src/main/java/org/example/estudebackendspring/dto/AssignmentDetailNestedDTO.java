package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.estudebackendspring.enums.AssignmentType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentDetailNestedDTO {
    private Long assignmentId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private LocalDateTime startDate;
    private LocalDateTime createdAt;
    private AssignmentType type;
    private Float maxScore;
    private Integer timeLimit;
    private Boolean isPublished;
    private Boolean allowLateSubmission;
    private Float latePenalty;
    private Integer submissionLimit;
    private String attachmentUrl;
    private ClassSubjectNestedDTO classSubject;
    private List<TopicSimpleDTO> topics;
    private List<QuestionDTO> questions;
}
