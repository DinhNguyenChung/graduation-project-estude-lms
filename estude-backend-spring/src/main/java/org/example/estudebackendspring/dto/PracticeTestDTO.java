package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeTestDTO {
    private Long testId;
    private String title;
    private Long subjectId;
    private String subjectName;
    private Long studentId;
    private String studentName;
    private String testType;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Integer totalQuestions;
    private Integer timeLimit;
    private List<TopicDTO> selectedTopics;
    private List<QuestionDTO> questions;
    private Boolean isCompleted; // Đã làm chưa
    private Long submissionId; // Nếu đã làm
}
