package org.example.estudebackendspring.dto.learning;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImprovementResponse {
    private Boolean success;
    private ImprovementDataDTO data;
}
