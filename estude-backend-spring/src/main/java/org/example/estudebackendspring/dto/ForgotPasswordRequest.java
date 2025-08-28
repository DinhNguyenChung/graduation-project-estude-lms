package org.example.estudebackendspring.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "Email or phone number is required")
    private String email;
}
