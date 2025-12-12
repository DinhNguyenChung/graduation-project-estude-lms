package org.example.estudebackendspring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.Assignment;
import org.example.estudebackendspring.entity.Submission;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.enums.ActionType;
import org.example.estudebackendspring.repository.UserRepository;
import org.example.estudebackendspring.service.AssignmentSubmissionService;
import org.example.estudebackendspring.service.LogEntryService;
import org.example.estudebackendspring.service.SubmissionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class SubmissionController {
    private final SubmissionService submissionService;
    private final AssignmentSubmissionService assignmentSubmissionService;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final LogEntryService logEntryService;
    private final UserRepository userRepository;

    public SubmissionController(SubmissionService submissionService, AssignmentSubmissionService assignmentSubmissionService,
                                ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate, LogEntryService logEntryService, UserRepository userRepository) {
        this.submissionService = submissionService;
        this.assignmentSubmissionService = assignmentSubmissionService;
        this.objectMapper = new ObjectMapper();
        this.messagingTemplate = messagingTemplate;
        this.logEntryService = logEntryService;
        this.userRepository = userRepository;
    }
//    @GetMapping("/submissions")
//    public List<Submission> getSubmissions() {
//        return submissionService.getAllSubmissions();
//    }
    @PostMapping(value = "/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubmissionResultDTO> submitAssignment(
            @RequestPart("submission") String submissionJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws Exception {
        SubmissionRequest req = objectMapper.readValue(submissionJson, SubmissionRequest.class);
        SubmissionResultDTO res = assignmentSubmissionService.submitAssignment(req, files == null ? Collections.emptyList() : files);
        
        // Log submission creation
        try {
            SubmissionDTO submission = submissionService.getSubmission(res.getSubmissionId())
                    .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
            User user = userRepository.findById(submission.getStudentId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid actingUserId"));
            int fileCount = files != null ? files.size() : 0;
            String fileInfo = fileCount > 0 ? " (kèm " + fileCount + " file)" : "";
//            logEntryService.createLog(
//                "Submission",
//                res.getSubmissionId(),
//                "Học sinh nộp bài tập: " + submission.getAssignmentName() + fileInfo,
//                ActionType.CREATE,
//                req.getAssignmentId(),
//                "Assignment",
//                user
//            );
        } catch (Exception e) {
            // Log warning but don't fail the main operation
            System.err.println("Failed to log submission: " + e.getMessage());
        }
        
        // Gửi thông báo WebSocket cho FE (các client subscribe)
        messagingTemplate.convertAndSend(
                "/topic/assignment/" + req.getAssignmentId() + "/submissions",
                res
        );
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<?> getSubmissionsByClassSubject(@PathVariable Long classSubjectId) {
        List<SubmissionResponseDTO> submissions = submissionService.getSubmissionsByClassSubject(classSubjectId);
        return ResponseEntity.ok(new AuthResponse(true, "Submissions retrieved", submissions));
    }

    @GetMapping("/submissions/{submissionId}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<AuthResponse> getSubmission(@PathVariable Long submissionId) {
        try {
            Optional<SubmissionDTO> submission = submissionService.getSubmission(submissionId);
            if (submission.isPresent()) {
                return ResponseEntity.ok(new AuthResponse(true, "Submission retrieved", submission.get()));
            } else {
                return ResponseEntity.ok(new AuthResponse(false, "Submission not found", null));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }


    //    @GetMapping("/submissions/{submissionId}/assignment")
//    public ResponseEntity<AssignmentDTO> getAssignmentBySubmission(@PathVariable Long submissionId) {
//        return ResponseEntity.ok(submissionService.getAssignmentBySubmission(submissionId));
//    }
    @GetMapping("/submissions/student/{studentId}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<SubmissionDTO>> getSubmissionsByStudent(@PathVariable Long studentId) {
        List<SubmissionDTO> submissions = submissionService.getSubmissionsByStudent(studentId);
        return ResponseEntity.ok(submissions);
    }
    // Lấy tất cả submission theo student + assignment
    @GetMapping("/submissions/student/{studentId}/assignment/{assignmentId}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<SubmissionDTO>> getSubmissionsByStudentAndAssignment(
            @PathVariable Long studentId,
            @PathVariable Long assignmentId) {
        if (studentId == null || studentId <= 0 || assignmentId == null || assignmentId <= 0) {
            return ResponseEntity.badRequest().body(null);
        }
        List<SubmissionDTO> submissions = submissionService.getSubmissionsByStudentAndAssignment(studentId, assignmentId);
        if (submissions.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        return ResponseEntity.ok(submissions);
    }


}
