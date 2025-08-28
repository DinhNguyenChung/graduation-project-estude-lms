package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdatePasswordResponse {
    private boolean success;
    private String message;
    private String token; // JWT mới (null nếu bạn không muốn trả)
}