package org.example.estudebackendspring.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.estudebackendspring.enums.UserRole;

@Setter
@Getter
@Data
public class UserDTO {
    private Long userId;
    private String fullName;
    private String email;
    private UserRole role;

    public UserDTO(Long userId, String fullName, String email, UserRole role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    // Getters & Setters
}
