package org.example.estudebackendspring.service;

import org.example.estudebackendspring.dto.AssignmentResponseDTO;
import org.example.estudebackendspring.entity.Assignment;
import org.example.estudebackendspring.repository.AssignmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete assignment with submissions");
        }
        assignmentRepository.delete(existing);
    }
    public List<Assignment> getAssignmentsByClass(Long classId) {
        return assignmentRepository.findByClassId(classId);
    }
    public List<Assignment> getAssignmentsByClassSubject(Long subjectId) {
        return assignmentRepository.findAssignmentsByClassSubject_ClassSubjectId(subjectId);
    }
    
    /**
     * Convert Assignment entity to DTO to avoid lazy loading issues
     */
    @Transactional(readOnly = true)
    public AssignmentResponseDTO convertToDTO(Assignment assignment) {
        AssignmentResponseDTO dto = new AssignmentResponseDTO();
        dto.setAssignmentId(assignment.getAssignmentId());
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setDueDate(assignment.getDueDate());
        dto.setTimeLimit(assignment.getTimeLimit());
        dto.setType(assignment.getType());
        dto.setAttachmentUrl(assignment.getAttachmentUrl());
        dto.setMaxScore(assignment.getMaxScore());
        dto.setIsPublished(assignment.getIsPublished());
        dto.setAllowLateSubmission(assignment.getAllowLateSubmission());
        dto.setLatePenalty(assignment.getLatePenalty());
        dto.setSubmissionLimit(assignment.getSubmissionLimit());
        dto.setCreatedAt(assignment.getCreatedAt());
        dto.setUpdatedAt(assignment.getUpdatedAt());
        dto.setAnswerKeyFileUrl(assignment.getAnswerKeyFileUrl());
        dto.setIsAutoGraded(assignment.getIsAutoGraded());
        dto.setIsExam(assignment.getIsExam());
        dto.setStartDate(assignment.getStartDate());
        
        // Teacher info
        if (assignment.getTeacher() != null) {
            dto.setTeacherId(assignment.getTeacher().getUserId());
            dto.setTeacherName(assignment.getTeacher().getFullName());
        }
        
        // ClassSubject info
        if (assignment.getClassSubject() != null) {
            dto.setClassSubjectId(assignment.getClassSubject().getClassSubjectId());
            if (assignment.getClassSubject().getSubject() != null) {
                dto.setSubjectName(assignment.getClassSubject().getSubject().getName());
            }
            if (assignment.getClassSubject().getTerm() != null && 
                assignment.getClassSubject().getTerm().getClazz() != null) {
                dto.setClassName(assignment.getClassSubject().getTerm().getClazz().getName());
            }
        }
        
        return dto;
    }
}