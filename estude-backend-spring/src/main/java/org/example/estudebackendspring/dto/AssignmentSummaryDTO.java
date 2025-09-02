package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentSummaryDTO {
    private Long assignmentId;
    private String title;
    private LocalDateTime dueDate;
    private Long classId;
    private String className;
}
