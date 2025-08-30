package org.example.estudebackendspring.service;

import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Optional<Student> getStudentById(Long studentId) {
        return Optional.ofNullable(studentRepository.findByUserId(studentId));
    }
}