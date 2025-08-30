package org.example.estudebackendspring.controller;


import org.example.estudebackendspring.dto.ApiResponse;
import org.example.estudebackendspring.dto.AuthResponse;
import org.example.estudebackendspring.entity.Assignment;
import org.example.estudebackendspring.service.AssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public ResponseEntity<?> createAssignment(@RequestBody Assignment assignment) {
        Assignment created = assignmentService.createAssignment(assignment);
        return ResponseEntity.ok(
                new AuthResponse(true, "Assignment created successfully", created)
        );
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
}