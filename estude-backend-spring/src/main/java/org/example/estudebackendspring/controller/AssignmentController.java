package org.example.estudebackendspring.controller;


import org.example.estudebackendspring.dto.ApiResponse;
import org.example.estudebackendspring.dto.AssignmentDetailDTO;
import org.example.estudebackendspring.dto.AuthResponse;
import org.example.estudebackendspring.entity.Assignment;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.repository.AssignmentRepository;
import org.example.estudebackendspring.repository.ClassSubjectRepository;
import org.example.estudebackendspring.repository.TeacherRepository;
import org.example.estudebackendspring.service.AssignmentService;
import org.example.estudebackendspring.service.AssignmentSubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionService assignmentSubmissionService;
    private final TeacherRepository teacherRepository;
    private final ClassSubjectRepository classSubjectRepository;

    public AssignmentController(AssignmentService assignmentService , AssignmentRepository assignmentRepository,
                                AssignmentSubmissionService assignmentSubmissionService, TeacherRepository teacherRepository, ClassSubjectRepository classSubjectRepository) {
        this.assignmentService = assignmentService;
        this.assignmentRepository = assignmentRepository;
        this.assignmentSubmissionService = assignmentSubmissionService;
        this.teacherRepository = teacherRepository;
        this.classSubjectRepository = classSubjectRepository;
    }
    @GetMapping
    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }
    @PostMapping
    public ResponseEntity<?> createAssignment(@RequestBody Assignment assignment) {
        try {
            // Lấy teacher từ DB
            Teacher teacher = teacherRepository.findById(assignment.getTeacher().getUserId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // Lấy classSubject từ DB
            ClassSubject classSubject = classSubjectRepository.findById(assignment.getClassSubject().getClassSubjectId())
                    .orElseThrow(() -> new RuntimeException("ClassSubject not found"));

            // Gắn teacher và classSubject vào assignment
            assignment.setTeacher(teacher);
            assignment.setClassSubject(classSubject);

            // Lưu assignment
            Assignment created = assignmentService.createAssignment(assignment);

            return ResponseEntity.ok(
                    new AuthResponse(true, "Assignment created successfully", created)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Unexpected error", null));
        }
    }


    // GET /api/assignments/{assignmentId}
    @GetMapping("/{assignmentId}")
    public ResponseEntity<?> getAssignment(@PathVariable Long assignmentId) {
        try {
            Assignment assignment = assignmentService.getAssignment(assignmentId);
            return ResponseEntity.ok(
                    new AuthResponse(true, "Assignment found", assignment)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.ok(
                    new AuthResponse(false, "Assignment not found", null)
            );
        }
    }

    // PUT /api/assignments/{assignmentId}
    @PutMapping("/{assignmentId}")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestBody Assignment updated) {
        try {
            Assignment assignment = assignmentService.updateAssignment(assignmentId, updated);
            return ResponseEntity.ok(
                    new AuthResponse(true, "Assignment updated successfully", assignment)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.ok(
                    new AuthResponse(false, "Failed to update assignment", null)
            );
        }
    }

    // DELETE /api/assignments/{assignmentId}
    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long assignmentId) {
        try {
            assignmentService.deleteAssignment(assignmentId);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Assignment deleted successfully")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.ok(
                    new ApiResponse(false, "Failed to delete assignment")
            );
        }
    }
//    @GetMapping("/{assignmentId}")
//    public ResponseEntity<AssignmentDetailDTO> getAssignmentDetail(@PathVariable Long assignmentId) {
//        AssignmentDetailDTO dto = assignmentSubmissionService.getAssignmentDetail(assignmentId);
//        return ResponseEntity.ok(dto);
//    }
}