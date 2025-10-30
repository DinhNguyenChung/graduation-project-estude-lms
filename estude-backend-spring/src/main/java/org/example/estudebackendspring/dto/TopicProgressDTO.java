package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicProgressDTO {
    private Long progressId;
    private Long studentId;
    private String studentName;
    private Long topicId;
    private String topicName;
    private Long submissionId;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Float accuracyRate; // 0.0 - 1.0 (0.6 = 60%)
    private LocalDateTime recordedAt;
}
