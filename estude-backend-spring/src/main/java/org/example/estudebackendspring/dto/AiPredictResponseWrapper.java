package org.example.estudebackendspring.dto;

import lombok.Data;

@Data
public class AiPredictResponseWrapper {
    private boolean success;
    private AiPredictResponse data;
}
