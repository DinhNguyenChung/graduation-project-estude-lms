package org.example.estudebackendspring.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateClazzRequest {
    @NotBlank
    private String name;
    private String term;
    @Min(0)
    private Integer classSize;

}