package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicProgressSummaryDTO {
    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectName;
    private List<TopicStatDTO> topicStats;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicStatDTO {
        private Long topicId;
        private String topicName;
        private Integer attemptCount; // Số lần làm bài có topic này
        private Float averageAccuracy; // Trung bình % đúng
        private Float latestAccuracy; // % đúng lần gần nhất
        private Float trend; // Xu hướng: +0.2 = cải thiện 20%
        private String status; // "WEAK", "IMPROVING", "GOOD", "EXCELLENT"
    }
}
