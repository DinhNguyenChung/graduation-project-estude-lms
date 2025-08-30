package org.example.estudebackendspring.dto;

import lombok.Data;

@Data
public class GradeUpdateRequest {
    private Float score;
    private String feedback;
}
