package org.example.estudebackendspring.service;

import org.example.estudebackendspring.dto.AssignmentDTO;
import org.example.estudebackendspring.entity.Assignment;
import org.example.estudebackendspring.entity.Submission;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.SubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;

    public SubmissionService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    public List<Submission> getSubmissionsByClassSubject(Long classSubjectId) {
        return submissionRepository.findByAssignment_ClassSubject_ClassSubjectId(classSubjectId);
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

}