package org.example.estudebackendspring.mapper;

import org.example.estudebackendspring.dto.SchoolDTO;
import org.example.estudebackendspring.dto.UserDTO;
import org.example.estudebackendspring.entity.Admin;
import org.example.estudebackendspring.entity.School;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.Teacher;
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
        
        // Map role-specific code based on user type
        if (user instanceof Admin) {
            dto.setAdminCode(((Admin) user).getAdminCode());
        } else if (user instanceof Teacher) {
            Teacher teacher = (Teacher) user;
            dto.setTeacherCode(teacher.getTeacherCode());
            dto.setIsAdmin(teacher.isAdmin());
            dto.setHomeroomTeacher(teacher.isHomeroomTeacher());
        } else if (user instanceof Student) {
            dto.setStudentCode(((Student) user).getStudentCode());
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
