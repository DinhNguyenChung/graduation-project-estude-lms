package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentDetailDTO {
    private Long assignmentId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private List<QuestionDTO> questions;
}