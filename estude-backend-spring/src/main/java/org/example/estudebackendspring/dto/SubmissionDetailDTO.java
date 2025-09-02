package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionDetailDTO {
    private Long submissionId;
    private LocalDateTime submittedAt;
    private String status;
    private Float score;
    private String aiFeedback;
    private List<AnswerDetailDTO> answers;
}
