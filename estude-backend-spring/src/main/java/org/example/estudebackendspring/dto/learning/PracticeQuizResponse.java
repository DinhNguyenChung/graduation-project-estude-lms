package org.example.estudebackendspring.dto.learning;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeQuizResponse {
    private Boolean success;
    private PracticeQuizDataDTO data;
}
