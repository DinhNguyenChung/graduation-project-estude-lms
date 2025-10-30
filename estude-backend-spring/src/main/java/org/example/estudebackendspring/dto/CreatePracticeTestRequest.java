package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePracticeTestRequest {
    private Long subjectId;
    private Long studentId;
    private List<Long> topicIds; // Danh sách topic IDs cần kiểm tra
    private Integer numQuestions; // Tổng số câu hỏi
    private String difficultyLevel; // "EASY", "MEDIUM", "HARD", "MIXED"
    private Integer timeLimit; // Phút (optional)
}
