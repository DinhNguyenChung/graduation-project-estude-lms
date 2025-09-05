package org.example.estudebackendspring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.Assignment;
import org.example.estudebackendspring.entity.Submission;
import org.example.estudebackendspring.service.AssignmentSubmissionService;
import org.example.estudebackendspring.service.SubmissionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SubmissionController {
    private final SubmissionService submissionService;
    private final AssignmentSubmissionService assignmentSubmissionService;
    private final ObjectMapper objectMapper;
    public SubmissionController(SubmissionService submissionService, AssignmentSubmissionService assignmentSubmissionService) {
        this.submissionService = submissionService;
        this.assignmentSubmissionService = assignmentSubmissionService;
        this.objectMapper = new ObjectMapper();
    }
    @GetMapping("/submissions")
    public List<Submission> getSubmissions() {
        return submissionService.getAllSubmissions();
    }
    @PostMapping(value = "/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubmissionResultDTO> submitAssignment(
            @RequestPart("submission") String submissionJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws Exception {
        SubmissionRequest req = objectMapper.readValue(submissionJson, SubmissionRequest.class);
        SubmissionResultDTO res = assignmentSubmissionService.submitAssignment(req, files == null ? Collections.emptyList() : files);
        return ResponseEntity.ok(res);
    }
//    @GetMapping("/submissions/{submissionId}")
//    public ResponseEntity<SubmissionDetailDTO> getSubmission(@PathVariable Long submissionId) {
//        SubmissionDetailDTO dto = assignmentSubmissionService.getSubmissionDetail(submissionId);
//        return ResponseEntity.ok(dto);
//    }

//    @GetMapping("/class-subjects/{classSubjectId}/submissions")
//    public ResponseEntity<?> getSubmissionsByClassSubject(@PathVariable Long classSubjectId) {
//        List<Submission> submissions = submissionService.getSubmissionsByClassSubject(classSubjectId);
//        return ResponseEntity.ok(new AuthResponse(true, "Submissions retrieved", submissions));
//    }
    @GetMapping("/class-subjects/{classSubjectId}/submissions")
    public ResponseEntity<?> getSubmissionsByClassSubject(@PathVariable Long classSubjectId) {
        List<SubmissionResponseDTO> submissions = submissionService.getSubmissionsByClassSubject(classSubjectId);
        return ResponseEntity.ok(new AuthResponse(true, "Submissions retrieved", submissions));
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<?> getSubmission(@PathVariable Long submissionId) {
        try {
            Submission submission = submissionService.getSubmission(submissionId);
            return ResponseEntity.ok(new AuthResponse(true, "Submission retrieved", submission));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
    @GetMapping("/submissions/{submissionId}/assignment")
    public ResponseEntity<AssignmentDTO> getAssignmentBySubmission(@PathVariable Long submissionId) {
        return ResponseEntity.ok(submissionService.getAssignmentBySubmission(submissionId));
    }


}
