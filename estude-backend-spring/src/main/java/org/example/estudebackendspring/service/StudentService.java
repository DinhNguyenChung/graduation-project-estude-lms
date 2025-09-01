package org.example.estudebackendspring.service;

import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public List<Student> getStudentsByClass(Long classId) {
        return studentRepository.findStudentsByClassId(classId);
    }

    public List<Student> getStudentsBySchool(Long schoolId) {
        return studentRepository.findStudentsBySchoolId(schoolId);
    }
}