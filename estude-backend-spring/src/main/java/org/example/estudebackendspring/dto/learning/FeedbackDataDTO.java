package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDataDTO {
    @JsonProperty("assignment_id")
    private String assignmentId;
    
    @JsonProperty("student_name")
    private String studentName;
    
    private String subject;
    private LocalDateTime timestamp;
    private FeedbackSummaryDTO summary;
    private List<QuestionFeedbackDTO> feedback;
}
