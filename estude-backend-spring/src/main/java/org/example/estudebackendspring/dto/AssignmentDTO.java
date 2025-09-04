package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentDTO {
    private Long assignmentId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
}
