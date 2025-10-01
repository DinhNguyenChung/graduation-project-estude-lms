package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.estudebackendspring.enums.AssignmentType;
import org.example.estudebackendspring.enums.SubmissionStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentSummaryDTO {
    private Long assignmentId;
    private String title;
    private LocalDateTime dueDate;
    private Long classId;
    private String className;
    private Boolean isExam;
    private AssignmentType type;
    private Integer submissionLimit;
    private Boolean allowLateSubmission;
    private SubmissionStatus status;
}
