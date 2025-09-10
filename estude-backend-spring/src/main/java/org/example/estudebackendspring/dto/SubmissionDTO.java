package org.example.estudebackendspring.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.estudebackendspring.enums.SubmissionStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
public class SubmissionDTO {
    private Long submissionId;
    private LocalDateTime submittedAt;
    private String fileUrl;
    private String content;
    private SubmissionStatus status;
    private Boolean isLate;
    private Integer attemptNumber;
    private LocalDateTime autoGradedAt;
    private Long studentId;
    private Long assignmentId;
    private String assignmentName;
    private Long classSubjectId;
    private String subjectName;
    private String className;
    private List<AnswerDTO> answers;
    private Long gradeId;
    private Float score;
    private String autoGradeFeedback;
}
