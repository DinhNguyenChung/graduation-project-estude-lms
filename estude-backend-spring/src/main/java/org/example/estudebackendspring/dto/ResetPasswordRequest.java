package org.example.estudebackendspring.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class ResetPasswordRequest {
    private String email;
    private String otp;
    private String newPassword;
}
