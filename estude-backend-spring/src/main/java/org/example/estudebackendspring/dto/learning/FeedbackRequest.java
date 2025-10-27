package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {
    @JsonProperty("submission_id")
    private String submissionId;
    
    @JsonProperty("assignment_id")
    private String assignmentId;
    
    @JsonProperty("student_name")
    private String studentName;
    
    private String subject;
    private List<QuestionDTO> questions;
}
