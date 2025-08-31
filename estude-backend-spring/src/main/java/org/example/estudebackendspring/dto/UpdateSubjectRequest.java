package org.example.estudebackendspring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateSubjectRequest {
    @NotBlank
    private String name;
    private String description;

}
