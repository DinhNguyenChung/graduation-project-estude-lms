package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.entity.Enrollment;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.repository.StudentRepository;
import org.example.estudebackendspring.service.EnrollmentService;
import org.example.estudebackendspring.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final EnrollmentService enrollmentService;
    private final StudentRepository repository;

    public StudentController(StudentService studentService, EnrollmentService enrollmentService, StudentRepository repository) {
        this.studentService = studentService;
        this.enrollmentService = enrollmentService;
        this.repository = repository;
    }
    @GetMapping
    public List<Student> getAllStudents() {
        return repository.findAll();
    }
    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudentById(@PathVariable Long studentId) {
        return studentService.getStudentById(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    //List classes the student is enrolled in
    @GetMapping("/{studentId}/enrollments")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByStudent(@PathVariable Long studentId) {
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/by-class/{classId}")
    public ResponseEntity<List<Student>> getStudentsByClass(@PathVariable Long classId) {
        return ResponseEntity.ok(studentService.getStudentsByClass(classId));
    }


    @GetMapping("/by-school/{schoolId}")
    public ResponseEntity<List<Student>> getStudentsBySchool(@PathVariable Long schoolId) {
        return ResponseEntity.ok(studentService.getStudentsBySchool(schoolId));
    }

}
