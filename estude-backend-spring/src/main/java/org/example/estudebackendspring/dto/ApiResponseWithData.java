package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper with data payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseWithData<T> {
    private boolean success;
    private String message;
    private T data;
}
