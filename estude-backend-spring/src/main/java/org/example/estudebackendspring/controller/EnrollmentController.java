package org.example.estudebackendspring.controller;


import jakarta.validation.Valid;
import org.example.estudebackendspring.dto.CreateEnrollmentRequest;
import org.example.estudebackendspring.entity.Enrollment;
import org.example.estudebackendspring.repository.EnrollmentRepository;
import org.example.estudebackendspring.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/enrollments")
@Validated
public class EnrollmentController {

    private final EnrollmentService service;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentController(EnrollmentService service, EnrollmentRepository enrollmentRepository) {
        this.service = service;
        this.enrollmentRepository = enrollmentRepository;
    }
    @GetMapping
    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

//    @PostMapping
//    public ResponseEntity<Enrollment> enrollStudent(@Valid @RequestBody CreateEnrollmentRequest req) {
//        Enrollment created = service.enrollStudent(req);
//        return ResponseEntity.status(HttpStatus.CREATED).body(created);
//    }

    @PostMapping
    public ResponseEntity<List<Enrollment>> enrollStudentsBatch(
            @RequestParam Long classId,
            @RequestBody List<Long> studentIds) {

        List<Enrollment> created = service.enrollStudents(classId, studentIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{enrollmentId}")
    public ResponseEntity<Void> removeEnrollment(@PathVariable Long enrollmentId) {
        service.removeEnrollment(enrollmentId);
        return ResponseEntity.noContent().build();
    }
//    @GetMapping("/student/{studentID}")
//    public ResponseEntity<List<Enrollment>> getStudentEnrollments(@PathVariable Long studentID) {
////        return service.getEnrollmentsByStudent(studentID).
//    }
}