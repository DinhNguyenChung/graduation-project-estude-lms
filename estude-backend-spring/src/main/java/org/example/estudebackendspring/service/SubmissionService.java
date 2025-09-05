package org.example.estudebackendspring.service;

import org.example.estudebackendspring.dto.AssignmentDTO;
import org.example.estudebackendspring.dto.SubmissionResponseDTO;
import org.example.estudebackendspring.entity.Assignment;
import org.example.estudebackendspring.entity.Submission;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.SubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;

    public SubmissionService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

//    public List<Submission> getSubmissionsByClassSubject(Long classSubjectId) {
//        return submissionRepository.findByAssignment_ClassSubject_ClassSubjectId(classSubjectId);
//    }
public List<SubmissionResponseDTO> getSubmissionsByClassSubject(Long classSubjectId) {
    List<Submission> submissions =
            submissionRepository.findByAssignment_ClassSubject_ClassSubjectId(classSubjectId);

    return submissions.stream().map(s -> {
        SubmissionResponseDTO dto = new SubmissionResponseDTO();
        // submission
        dto.setSubmissionId(s.getSubmissionId());
        dto.setSubmittedAt(s.getSubmittedAt());
        dto.setFileUrl(s.getFileUrl());
        dto.setContent(s.getContent());
        dto.setStatus(s.getStatus() != null ? s.getStatus().name() : null);
        dto.setIsLate(s.getIsLate());
        dto.setAttemptNumber(s.getAttemptNumber());
        dto.setAutoGradedAt(s.getAutoGradedAt());

        // student
        if (s.getStudent() != null) {
            dto.setStudentId(s.getStudent().getUserId());
            dto.setStudentCode(s.getStudent().getStudentCode());
            dto.setStudentName(s.getStudent().getFullName());
        }

        // assignment
        if (s.getAssignment() != null) {
            dto.setAssignmentId(s.getAssignment().getAssignmentId());
            dto.setAssignmentTitle(s.getAssignment().getTitle());
            dto.setDueDate(s.getAssignment().getDueDate());
        }

        // grade
        if (s.getGrade() != null) {
            dto.setGradeId(s.getGrade().getGradeId());
            dto.setScore(s.getGrade().getScore());
            dto.setGradeComment(s.getGrade().getAutoGradedFeedback());
        }

        return dto;
    }).collect(Collectors.toList());
}

    public Submission getSubmission(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
    }
    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }
    public AssignmentDTO getAssignmentBySubmission(Long submissionId) {
        Submission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + submissionId));

        Assignment a = sub.getAssignment();
        if (a == null) {
            throw new ResourceNotFoundException("Assignment not found for submission: " + submissionId);
        }

        return new AssignmentDTO(
                a.getAssignmentId(),
                a.getTitle(),
                a.getDescription(),
                a.getDueDate()
        );
    }
    public List<Submission> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudent_userId(studentId);
    }

    public List<Submission> getSubmissionsByStudentAndAssignment(Long studentId, Long assignmentId) {
        return submissionRepository.findByStudent_userIdAndAssignment_AssignmentId(studentId, assignmentId);
    }

}