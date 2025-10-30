package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicDTO {
    private Long topicId;
    private String name;
    private String description;
    private String chapter;
    private Integer orderIndex;
    private String gradeLevel; // "GRADE_10", "GRADE_11", "GRADE_12"
    private Integer volume; // 1, 2, 3... (Tập 1, Tập 2, Tập 3) - để phân biệt topic thuộc tập nào
    private Long subjectId;
    private String subjectName;
    private Integer totalQuestions; // Số câu hỏi trong question bank
}
