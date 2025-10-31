package org.example.estudebackendspring.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.service.AssessmentService;
import org.example.estudebackendspring.service.AssessmentSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Assessment Question Generation and Submission API
 * As per ASSESSMENT_API_DOCS.md specification
 */
@Slf4j
@RestController
@RequestMapping("/api/assessment")
@CrossOrigin(origins = "*")
public class AssessmentController {
    
    private final AssessmentService assessmentService;
    private final AssessmentSubmissionService assessmentSubmissionService;
    
    public AssessmentController(AssessmentService assessmentService,
                                AssessmentSubmissionService assessmentSubmissionService) {
        this.assessmentService = assessmentService;
        this.assessmentSubmissionService = assessmentSubmissionService;
    }
    
    /**
     * POST /api/assessment/generate-questions
     * 
     * Generate random assessment questions from Question Bank
     * 
     * Features:
     * - Even distribution across topics
     * - Mixed difficulty ratio (40% EASY, 40% MEDIUM, 20% HARD)
     * - Random shuffling
     * - Fallback if insufficient questions
     * 
     * @param request CreateAssessmentRequest with validation
     * @return AssessmentResponseDTO with questions and distribution stats
     */
    @PostMapping("/generate-questions")
    public ResponseEntity<ApiResponseWithData<AssessmentResponseDTO>> generateAssessmentQuestions(
            @Valid @RequestBody CreateAssessmentRequest request) {
        
        log.info("POST /api/assessment/generate-questions - Student: {}, Subject: {}, Topics: {}, Questions: {}, Difficulty: {}",
            request.getStudentId(), request.getSubjectId(), request.getTopicIds().size(), 
            request.getNumQuestions(), request.getDifficulty());
        
        // Authorization check: Verify studentId matches authenticated user
        // TODO: Implement when authentication is integrated
        // if (!authService.isCurrentUser(request.getStudentId())) {
        //     throw new ForbiddenException("Cannot generate assessment for other students");
        // }
        
        AssessmentResponseDTO response = assessmentService.generateAssessmentQuestions(request);
        
        return ResponseEntity.ok(new ApiResponseWithData<>(
            true,
            "Đã tạo bộ câu hỏi thành công",
            response
        ));
    }
    
    /**
     * POST /api/assessment/submit
     * 
     * Submit assessment answers and get auto-graded results
     * 
     * Features:
     * - Auto-grading with instant feedback
     * - Duplicate submission prevention
     * - Detailed answer breakdown (correct vs incorrect)
     * - Statistics by topic and difficulty
     * - Performance level assessment
     * 
     * @param request SubmitAssessmentRequest with student's answers
     * @return AssessmentSubmissionResponseDTO with score and detailed results
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponseWithData<AssessmentSubmissionResponseDTO>> submitAssessment(
            @Valid @RequestBody SubmitAssessmentRequest request) {
        
        log.info("POST /api/assessment/submit - Assessment: {}, Student: {}, Answers: {}",
            request.getAssessmentId(), request.getStudentId(), request.getAnswers().size());
        
        // Authorization check: Verify studentId matches authenticated user
        // TODO: Implement when authentication is integrated
        // if (!authService.isCurrentUser(request.getStudentId())) {
        //     throw new ForbiddenException("Cannot submit assessment for other students");
        // }
        
        AssessmentSubmissionResponseDTO response = assessmentSubmissionService.submitAssessment(request);
        
        log.info("Assessment submitted successfully - Submission ID: {}, Score: {}%, Performance: {}",
            response.getSubmissionId(), response.getScore(), response.getPerformanceLevel());
        
        return ResponseEntity.ok(new ApiResponseWithData<>(
            true,
            "Nộp bài thành công! Điểm số: " + String.format("%.1f", response.getScore()) + "%",
            response
        ));
    }
    
    /**
     * GET /api/assessment/student/{studentId}/submissions
     * 
     * Get all assessment submissions for a student
     * 
     * Optional Query Parameters:
     * - subjectId: Filter by specific subject
     * 
     * @param studentId The student's ID
     * @param subjectId Optional subject filter
     * @return List of assessment submission summaries
     */
    @GetMapping("/student/{studentId}/submissions")
    public ResponseEntity<ApiResponseWithData<List<AssessmentSubmissionSummaryDTO>>> getStudentSubmissions(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long subjectId) {
        
        log.info("GET /api/assessment/student/{}/submissions - Subject filter: {}", studentId, subjectId);
        
        // Authorization check: Verify studentId matches authenticated user or is admin/teacher
        // TODO: Implement when authentication is integrated
        // if (!authService.isCurrentUserOrHasRole(studentId, "TEACHER", "ADMIN")) {
        //     throw new ForbiddenException("Cannot view other students' submissions");
        // }
        
        List<AssessmentSubmissionSummaryDTO> submissions;
        if (subjectId != null) {
            submissions = assessmentSubmissionService.getStudentSubmissionsBySubject(studentId, subjectId);
        } else {
            submissions = assessmentSubmissionService.getStudentSubmissions(studentId);
        }
        
        return ResponseEntity.ok(new ApiResponseWithData<>(
            true,
            "Đã tải " + submissions.size() + " bài nộp",
            submissions
        ));
    }
    
    /**
     * GET /api/assessment/submissions/{submissionId}
     * 
     * Get detailed view of a specific submission
     * 
     * Includes:
     * - All questions and answers
     * - Correct vs incorrect breakdown
     * - Statistics by topic and difficulty
     * - Performance metrics
     * 
     * @param submissionId The submission's ID
     * @return Detailed submission response with all answers
     */
    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<ApiResponseWithData<AssessmentSubmissionResponseDTO>> getSubmissionDetail(
            @PathVariable Long submissionId) {
        
        log.info("GET /api/assessment/submissions/{}", submissionId);
        
        // Authorization check: Verify submission belongs to current user or is admin/teacher
        // TODO: Implement when authentication is integrated
        // AssessmentSubmission submission = assessmentSubmissionRepository.findById(submissionId)
        //     .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        // if (!authService.isCurrentUserOrHasRole(submission.getStudent().getUserId(), "TEACHER", "ADMIN")) {
        //     throw new ForbiddenException("Cannot view other students' submissions");
        // }
        
        AssessmentSubmissionResponseDTO response = assessmentSubmissionService.getSubmissionDetail(submissionId);
        
        return ResponseEntity.ok(new ApiResponseWithData<>(
            true,
            "Đã tải chi tiết bài nộp",
            response
        ));
    }
    
    /**
     * GET /api/assessment/student/{studentId}/latest-submission
     * 
     * Get detailed view of the latest (most recent) assessment submission for a student
     * 
     * Optional Query Parameters:
     * - subjectId: Filter by specific subject
     * 
     * Use cases:
     * - Xem lại bài làm gần nhất của học sinh
     * - Review submission sau khi nộp bài
     * - Dashboard hiển thị kết quả mới nhất
     * 
     * Includes:
     * - All questions and answers
     * - Correct vs incorrect breakdown
     * - Statistics by topic and difficulty
     * - Performance metrics
     * - Submission timestamp
     * 
     * @param studentId The student's ID
     * @param subjectId Optional subject filter
     * @return Detailed submission response with all answers, or 404 if no submissions found
     */
    @GetMapping("/student/{studentId}/latest-submission")
    public ResponseEntity<ApiResponseWithData<AssessmentSubmissionResponseDTO>> getLatestSubmission(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long subjectId) {
        
        log.info("GET /api/assessment/student/{}/latest-submission - Subject filter: {}", studentId, subjectId);
        
        // Authorization check: Verify studentId matches authenticated user or is admin/teacher
        // TODO: Implement when authentication is integrated
        // if (!authService.isCurrentUserOrHasRole(studentId, "TEACHER", "ADMIN")) {
        //     throw new ForbiddenException("Cannot view other students' submissions");
        // }
        
        AssessmentSubmissionResponseDTO response = assessmentSubmissionService.getLatestSubmission(studentId, subjectId);
        
        return ResponseEntity.ok(new ApiResponseWithData<>(
            true,
            "Đã tải chi tiết bài nộp mới nhất",
            response
        ));
    }
    
    /**
     * GET /api/assessment/student/{studentId}/topic-statistics
     * 
     * Get topic-wise statistics for a student
     * 
     * Shows accuracy percentage for each topic across all assessment submissions
     * 
     * Optional Query Parameters:
     * - subjectId: Filter by specific subject
     * 
     * Example Response:
     * [
     *   {
     *     "topic": "Mệnh đề",
     *     "total_questions": 25,
     *     "correct_answers": 15,
     *     "accuracy": 0.6,
     *     "assessment_count": 5
     *   },
     *   {
     *     "topic": "Hàm số",
     *     "total_questions": 30,
     *     "correct_answers": 24,
     *     "accuracy": 0.8,
     *     "assessment_count": 6
     *   }
     * ]
     * 
     * @param studentId The student's ID
     * @param subjectId Optional subject filter
     * @return List of topic statistics with accuracy percentages
     */
    @GetMapping("/student/{studentId}/topic-statistics")
    public ResponseEntity<ApiResponseWithData<List<TopicStatisticsDTO>>> getStudentTopicStatistics(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long subjectId) {
        
        log.info("GET /api/assessment/student/{}/topic-statistics - Subject filter: {}", studentId, subjectId);
        
        // Authorization check: Verify studentId matches authenticated user or is admin/teacher
        // TODO: Implement when authentication is integrated
        // if (!authService.isCurrentUserOrHasRole(studentId, "TEACHER", "ADMIN")) {
        //     throw new ForbiddenException("Cannot view other students' statistics");
        // }
        
        List<TopicStatisticsDTO> statistics = assessmentSubmissionService.getStudentTopicStatistics(studentId, subjectId);
        
        String message = subjectId != null 
            ? "Đã tải thống kê " + statistics.size() + " topic cho môn học"
            : "Đã tải thống kê " + statistics.size() + " topic tổng hợp";
        
        return ResponseEntity.ok(new ApiResponseWithData<>(
            true,
            message,
            statistics
        ));
    }
    
    /**
     * PUT /api/assessment/submissions/{submissionId}/mark-evaluated
     * 
     * Mark submission as improvement evaluated (Layer 4)
     * 
     * Used by AI service after running improvement evaluation to prevent duplicate analysis.
     * Sets improvementEvaluated flag to true.
     * 
     * @param submissionId The submission's ID
     * @return Success message
     */
    @PutMapping("/submissions/{submissionId}/mark-evaluated")
    public ResponseEntity<ApiResponseWithData<String>> markSubmissionAsEvaluated(
            @PathVariable Long submissionId) {
        
        log.info("PUT /api/assessment/submissions/{}/mark-evaluated", submissionId);
        
        // Authorization check: Typically only AI service or admin should call this
        // TODO: Implement authorization when integrated
        
        assessmentSubmissionService.markAsImprovementEvaluated(submissionId);
        
        return ResponseEntity.ok(new ApiResponseWithData<>(
            true,
            "Đã đánh dấu bài nộp đã được đánh giá tiến bộ",
            "Submission " + submissionId + " marked as evaluated"
        ));
    }
    
    /**
     * PUT /api/assessment/submissions/{submissionId}/reset-evaluated
     * 
     * Reset improvement evaluated flag
     * 
     * Allows re-evaluation of a submission if needed.
     * Sets improvementEvaluated flag back to false.
     * 
     * @param submissionId The submission's ID
     * @return Success message
     */
    @PutMapping("/submissions/{submissionId}/reset-evaluated")
    public ResponseEntity<ApiResponseWithData<String>> resetSubmissionEvaluatedFlag(
            @PathVariable Long submissionId) {
        
        log.info("PUT /api/assessment/submissions/{}/reset-evaluated", submissionId);
        
        // Authorization check: Only admin/teacher should be able to reset
        // TODO: Implement authorization when integrated
        
        assessmentSubmissionService.resetImprovementEvaluated(submissionId);
        
        return ResponseEntity.ok(new ApiResponseWithData<>(
            true,
            "Đã reset cờ đánh giá tiến bộ",
            "Submission " + submissionId + " evaluation flag reset"
        ));
    }
}
