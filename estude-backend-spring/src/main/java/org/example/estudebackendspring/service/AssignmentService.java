package org.example.estudebackendspring.service;

import org.example.estudebackendspring.entity.Assignment;
import org.example.estudebackendspring.repository.AssignmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;

    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    // Create
    public Assignment createAssignment(Assignment assignment) {
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        return assignmentRepository.save(assignment);
    }

    // Read
    public Assignment getAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài tập"));
    }

    // Update
    public Assignment updateAssignment(Long assignmentId, Assignment updated) {
        Assignment existing = getAssignment(assignmentId);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDueDate(updated.getDueDate());
        existing.setMaxScore(updated.getMaxScore());
        existing.setIsPublished(updated.getIsPublished());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setAllowLateSubmission(updated.getAllowLateSubmission());
        existing.setLatePenalty(updated.getLatePenalty());
        existing.setSubmissionLimit(updated.getSubmissionLimit());
        existing.setAnswerKeyFileUrl(updated.getAnswerKeyFileUrl());
        existing.setIsAutoGraded(updated.getIsAutoGraded());
        return assignmentRepository.save(existing);
    }

    // Delete
    public void deleteAssignment(Long assignmentId) {
        Assignment existing = getAssignment(assignmentId);
        if (existing.getSubmissions() != null && !existing.getSubmissions().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể xóa bài tập đã nộp");
        }
        assignmentRepository.delete(existing);
    }
    public List<Assignment> getAssignmentsByClass(Long classId) {
        return assignmentRepository.findByClassId(classId);
    }
    public List<Assignment> getAssignmentsByClassSubject(Long subjectId) {
        return assignmentRepository.findAssignmentsByClassSubject_ClassSubjectId(subjectId);
    }
}