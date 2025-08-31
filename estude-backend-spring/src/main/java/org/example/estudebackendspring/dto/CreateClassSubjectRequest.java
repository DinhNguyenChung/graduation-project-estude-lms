package org.example.estudebackendspring.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateClassSubjectRequest {
    @NotNull
    private Long classId;
    @NotNull
    private Long subjectId;
    private Long teacherId; // optional
}
