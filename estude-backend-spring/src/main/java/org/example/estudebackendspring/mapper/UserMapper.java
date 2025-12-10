package org.example.estudebackendspring.mapper;

import org.example.estudebackendspring.dto.SchoolDTO;
import org.example.estudebackendspring.dto.UserDTO;
import org.example.estudebackendspring.entity.School;
import org.example.estudebackendspring.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setNumberPhone(user.getNumberPhone());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setAvatarPath(user.getAvatarPath());
        dto.setDob(user.getDob());
        dto.setRole(user.getRole());
        
        // Map school information
        if (user.getSchool() != null) {
            dto.setSchool(toSchoolDTO(user.getSchool()));
        }
        
        return dto;
    }
    
    private SchoolDTO toSchoolDTO(School school) {
        if (school == null) {
            return null;
        }
        
        SchoolDTO dto = new SchoolDTO();
        dto.setSchoolId(school.getSchoolId());
        dto.setSchoolCode(school.getSchoolCode());
        dto.setSchoolName(school.getSchoolName());
        dto.setAddress(school.getAddress());
        dto.setContactEmail(school.getContactEmail());
        dto.setContactPhone(school.getContactPhone());
        
        return dto;
    }
}
