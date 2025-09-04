package org.example.estudebackendspring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateSubjectRequest {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private Long schoolId;
}
