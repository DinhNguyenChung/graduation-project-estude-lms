package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.estudebackendspring.enums.UserRole;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String numberPhone;
    private String email;
    private String fullName;
    private String avatarPath;
    private Date dob;
    private SchoolDTO school;
    private UserRole role;
}
