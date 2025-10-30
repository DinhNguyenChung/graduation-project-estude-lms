package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionWithTopicsDTO {
    // Submission info
    private Long submissionId;
    private LocalDateTime submittedAt;
    private Float totalScore;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Float overallAccuracy;
    
    // Topic breakdown
    private List<TopicResultDTO> topicResults;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicResultDTO {
        private Long topicId;
        private String topicName;
        private Integer totalQuestions;
        private Integer correctAnswers;
        private Float accuracyRate; // 0.6 = 60%
        private String status; // "EXCELLENT", "GOOD", "NEED_IMPROVEMENT", "WEAK"
    }
}
