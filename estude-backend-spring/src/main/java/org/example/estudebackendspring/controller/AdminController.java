package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.service.AdminService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/create-student")
    public Student createStudent(@RequestParam Long schoolId,
                                 @RequestParam String studentCode,
                                 @RequestParam String fullName,
                                 @RequestParam String email,
                                 @RequestParam String phone,
                                 @RequestParam String password,
                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dob) {
        return adminService.createStudent(schoolId, studentCode, fullName, email, phone, password, dob);
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
        return adminService.createTeacher(schoolId, teacherCode, fullName, email, phone, password, dob, isAdmin, isHomeroomTeacher);
    }
}
