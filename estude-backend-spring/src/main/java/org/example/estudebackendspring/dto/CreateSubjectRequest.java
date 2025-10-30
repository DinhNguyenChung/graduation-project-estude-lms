package org.example.estudebackendspring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.estudebackendspring.enums.GradeLevel;

@Data
@AllArgsConstructor
public class CreateSubjectRequest {
    @NotBlank
    private String name;
    private String description;
//    @NotNull
//    private Long schoolId;
}
