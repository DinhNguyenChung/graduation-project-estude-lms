package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionRequest {
    private Long assignmentId;
    private Long studentId;
    private List<AnswerRequest> answers;
    private String content; // optional text / notes
}
