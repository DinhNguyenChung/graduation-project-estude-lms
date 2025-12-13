package org.example.estudebackendspring.controller;


import jakarta.validation.Valid;
import org.example.estudebackendspring.dto.CreateEnrollmentRequest;
import org.example.estudebackendspring.dto.EnrollmentDTO;
import org.example.estudebackendspring.entity.Enrollment;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.enums.ActionType;
import org.example.estudebackendspring.repository.EnrollmentRepository;
import org.example.estudebackendspring.service.EnrollmentService;
import org.example.estudebackendspring.service.LogEntryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/enrollments")
@Validated
public class  EnrollmentController {

    private final EnrollmentService service;
    private final EnrollmentRepository enrollmentRepository;
    private final LogEntryService logEntryService;

    public EnrollmentController(EnrollmentService service, EnrollmentRepository enrollmentRepository, LogEntryService logEntryService) {
        this.service = service;
        this.enrollmentRepository = enrollmentRepository;
        this.logEntryService = logEntryService;
    }
    
    @GetMapping
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getAllEnrollments() {
        return enrollmentRepository.findAllWithDetails()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private EnrollmentDTO convertToDTO(Enrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setEnrollmentId(enrollment.getEnrollmentId());
        dto.setDateJoined(enrollment.getDateJoined());
        
        // Student info
        if (enrollment.getStudent() != null) {
            dto.setStudentId(enrollment.getStudent().getUserId());
            dto.setStudentCode(enrollment.getStudent().getStudentCode());
            dto.setStudentName(enrollment.getStudent().getFullName());
            dto.setStudentEmail(enrollment.getStudent().getEmail());
        }
        
        // Class info
        if (enrollment.getClazz() != null) {
            dto.setClassId(enrollment.getClazz().getClassId());
            dto.setClassName(enrollment.getClazz().getName());
            dto.setGradeLevel(enrollment.getClazz().getGradeLevel() != null ? 
                    enrollment.getClazz().getGradeLevel().name() : null);
        }
        
        return dto;
    }

//    @PostMapping
//    public ResponseEntity<Enrollment> enrollStudent(@Valid @RequestBody CreateEnrollmentRequest req) {
//        Enrollment created = service.enrollStudent(req);
//        return ResponseEntity.status(HttpStatus.CREATED).body(created);
//    }

    @PostMapping
    @Transactional
    public ResponseEntity<List<EnrollmentDTO>> enrollStudentsBatch(
            @RequestParam Long classId,
            @RequestBody List<Long> studentIds) {

        List<Enrollment> created = service.enrollStudents(classId, studentIds);
        
        // Log batch enrollment
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                User currentUser = (User) principal;
                // Log individual enrollments
                for (Enrollment enrollment : created) {
                    logEntryService.createLog(
                        "Enrollment",
                        enrollment.getEnrollmentId(),
                        "Học sinh được đăng ký vào lớp",
                        ActionType.CREATE,
                        classId,
                        "Clazz",
                        currentUser
                    );
                }
            }
        } catch (Exception e) {
            // Log warning but don't fail the main operation
            System.err.println("Failed to log enrollment: " + e.getMessage());
        }
        
        // Convert to DTOs before returning
        List<EnrollmentDTO> dtos = created.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }

    @DeleteMapping("/{enrollmentId}")
    public ResponseEntity<Void> removeEnrollment(@PathVariable Long enrollmentId) {
        // Get enrollment info before deletion for logging
        Enrollment enrollment = null;
        try {
            enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
        } catch (Exception e) {
            // Continue with deletion even if we can't get enrollment info
        }
        
        service.removeEnrollment(enrollmentId);
        
        // Log enrollment removal
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (enrollment != null) {
                logEntryService.createLog(
                    "Enrollment",
                    enrollmentId,
                    "Hủy đăng ký học sinh khỏi lớp",
                    ActionType.DELETE,
                    enrollment.getClazz() != null ? enrollment.getClazz().getClassId() : null,
                    "Clazz",
                    currentUser
                );
            } else {
                logEntryService.createLog(
                    "Enrollment",
                    enrollmentId,
                    "Hủy đăng ký học sinh khỏi lớp",
                    ActionType.DELETE,
                    null,
                    "",
                    currentUser
                );
            }
        } catch (Exception e) {
            // Log warning but don't fail the main operation
            System.err.println("Failed to log enrollment removal: " + e.getMessage());
        }
        
        return ResponseEntity.noContent().build();
    }
//    @GetMapping("/student/{studentID}")
//    public ResponseEntity<List<Enrollment>> getStudentEnrollments(@PathVariable Long studentID) {
////        return service.getEnrollmentsByStudent(studentID).
//    }
}