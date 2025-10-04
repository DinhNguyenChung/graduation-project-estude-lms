package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.enums.ActionType;
import org.example.estudebackendspring.repository.UserRepository;
import org.example.estudebackendspring.service.AdminService;
import org.example.estudebackendspring.service.LogEntryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final LogEntryService logEntryService;
    private final UserRepository userRepository;

    public AdminController(AdminService adminService, LogEntryService logEntryService, UserRepository userRepository) {
        this.adminService = adminService;
        this.logEntryService = logEntryService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create-student")
    public Student createStudent(@RequestParam Long schoolId,
                                 @RequestParam String studentCode,
                                 @RequestParam String fullName,
                                 @RequestParam String email,
                                 @RequestParam String phone,
                                 @RequestParam String password,
                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dob) {
        Student student = adminService.createStudent(schoolId, studentCode, fullName, email, phone, password, dob);
        
        // Log student creation
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            logEntryService.createLog(
                "Student",
                student.getUserId(),
                "Admin tạo học sinh mới: " + fullName + " (" + studentCode + ")",
                ActionType.CREATE,
                schoolId,
                "School",
                    currentUser
            );
        } catch (Exception e) {
            // Log warning but don't fail the main operation
            System.err.println("Failed to log student creation: " + e.getMessage());
        }
        
        return student;
    }

    @PostMapping("/create-teacher")
    public Teacher createTeacher(@RequestParam Long schoolId,
                                 @RequestParam String teacherCode,
                                 @RequestParam String fullName,
                                 @RequestParam String email,
                                 @RequestParam String phone,
                                 @RequestParam String password,
                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dob,
                                 @RequestParam boolean isAdmin,
                                 @RequestParam boolean isHomeroomTeacher) {
        Teacher teacher = adminService.createTeacher(schoolId, teacherCode, fullName, email, phone, password, dob, isAdmin, isHomeroomTeacher);
        
        // Log teacher creation
        try {
            String role = isAdmin ? "(Admin)" : isHomeroomTeacher ? "(GVCN)" : "(Giáo viên)";
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            logEntryService.createLog(
                "Teacher",
                teacher.getUserId(),
                "Admin tạo giáo viên mới: " + fullName + " (" + teacherCode + ") " + role,
                ActionType.CREATE,
                schoolId,
                "School" ,
                    currentUser
            );
        } catch (Exception e) {
            // Log warning but don't fail the main operation
            System.err.println("Failed to log teacher creation: " + e.getMessage());
        }
        
        return teacher;
    }
}
