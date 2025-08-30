package org.example.estudebackendspring.service;

import org.example.estudebackendspring.entity.Submission;
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
}