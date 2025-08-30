package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.AuthResponse;
import org.example.estudebackendspring.entity.Submission;
import org.example.estudebackendspring.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SubmissionController {
    private final SubmissionService submissionService;
    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }
    // GET /api/class-subjects/{classSubjectId}/submissions
    @GetMapping("/class-subjects/{classSubjectId}/submissions")
    public ResponseEntity<?> getSubmissionsByClassSubject(@PathVariable Long classSubjectId) {
        List<Submission> submissions = submissionService.getSubmissionsByClassSubject(classSubjectId);
        return ResponseEntity.ok(new AuthResponse(true, "Submissions retrieved", submissions));
    }

    // GET /api/submissions/{submissionId}
    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<?> getSubmission(@PathVariable Long submissionId) {
        try {
            Submission submission = submissionService.getSubmission(submissionId);
            return ResponseEntity.ok(new AuthResponse(true, "Submission retrieved", submission));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
}
