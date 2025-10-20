package org.example.estudebackendspring.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.dto.learning.*;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.enums.AnalysisType;
import org.example.estudebackendspring.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Assignment API", description = "Quản lý Analysis tương tác Với AI Service ")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIAnalysisController {
    private final AIAnalysisService aiAnalysisService;
    private final SubjectAnalysisService subjectAnalysisService;
    private final SubmissionReportService submissionReportService;
    private final TestAnalysisService testAnalysisService;
    private final LearningLoopService learningLoopService;


    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Spring Boot API");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
//    public static record PredictReq(Long studentId) {}
    // Dự đoán học kỳ
    @GetMapping("/student/{studentId}/predict-semeter")
    public ResponseEntity<?> analyzePredict(@PathVariable Long studentId) {
//        if (body == null || body.studentId() == null) {
//            return ResponseEntity.badRequest().body("studentId is required");
//        }
        var result = aiAnalysisService.analyzePredict(studentId);
        return ResponseEntity.ok(result);
    }
    // Lấy kết quả dự đoán học kỳ mới nhất
    @GetMapping("/semester-latest/student/{studentId}")
    public ResponseEntity<AIAnalysisResult> getLatestResult(@PathVariable Long studentId) {
        AIAnalysisResult result = aiAnalysisService.getLatestResultByStudentId(
                studentId,
                AnalysisType.PREDICT_SEMESTER_PERFORMANCE
        );
        if (result == null) {
//            return ResponseEntity.notFound().build();
            return ResponseEntity.ok(new AIAnalysisResult());
        }
        return ResponseEntity.ok(result);
    }

    // Dự đoán điểm môn học
    @GetMapping("/analyze/{studentId}")
    public ResponseEntity<?> analyzeStudentSubjects(@PathVariable Long studentId) {
        try {
            JsonNode result = subjectAnalysisService.analyzeSubjectsAndSave(studentId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            // log lỗi
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Error analyzing subjects: " + ex.getMessage());
        }
    }
    // Lấy kết quả dự đoán điểm môn học mới nhất cả học sinh
    @GetMapping("/subject-latest/student/{studentId}")
    public ResponseEntity<AIAnalysisResult> getLatestResultSubjects(@PathVariable Long studentId) {
        AIAnalysisResult result = aiAnalysisService.getLatestResultByStudentId(
                studentId,
                AnalysisType.PREDICT_SUBJECT_GRADE
        );
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
    // Phân tích bài kiểm tra
    /**
     * Gọi AI để phân tích bài làm của học sinh (và lưu request/result).
     * GET tạm ổn cho thử nghiệm — nếu bạn muốn gửi payload custom, đổi thành POST.
     */
    @GetMapping("/analyze/{assignmentId}/student/{studentId}")
    public ResponseEntity<JsonNode> analyzeStudentSubmission(
            @PathVariable Long assignmentId,
            @PathVariable Long studentId) {
        try {
            JsonNode aiResponse = testAnalysisService.analyzeStudentSubmission(assignmentId, studentId);
            return ResponseEntity.ok(aiResponse);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception ex) {
            // log trong service đã in lỗi chi tiết
            return ResponseEntity.status(500).body(null);
        }
    }

    // Lấy bài kiểm tra
    @GetMapping("/assignment/{assignmentId}/student/{studentId}")
    public ResponseEntity<JsonNode> getStudentSubmission(
            @PathVariable Long assignmentId,
            @PathVariable Long studentId) {

        JsonNode result = submissionReportService.getStudentSubmission(assignmentId, studentId);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/student/{studentId}/assignment/{assignmentId}")
    public ResponseEntity<?> getLatestResult(
            @PathVariable Long studentId,
            @PathVariable String assignmentId
    ) {
        return aiAnalysisService.getLatestResult(studentId, assignmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== LEARNING LOOP AI ENDPOINTS ==========
    
    /**
     * Layer 1: Learning Feedback - Phân tích chi tiết từng câu hỏi
     * POST /api/ai/learning-feedback
     */
    @Operation(
            summary = "Layer 1: Learning Feedback - Phân tích chi tiết từng câu hỏi",
            description = "API này trả về phân tích chi tiết từng câu hỏi, xác định topic và giải thích lỗi sai .",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Thành công",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Assignment.class))
                    )
            }
    )
    @PostMapping("/learning-feedback")
    public ResponseEntity<FeedbackResponse> getLearningFeedback(@RequestBody FeedbackRequest request) {
        try {
            FeedbackResponse response = learningLoopService.getLearningFeedback(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    /**
     * Layer 2: Learning Recommendation - Đưa ra gợi ý học tập cá nhân hóa
     * POST /api/ai/learning-recommendation
     */
    @Operation(
            summary = "Layer 2: Learning Recommendation - Đưa ra gợi ý học tập cá nhân hóa",
            description = "API Đưa ra gợi ý học tập cá nhân hóa .",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Thành công",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Assignment.class))
                    )
            }
    )
    @PostMapping("/learning-recommendation")
    public ResponseEntity<RecommendationResponse> getLearningRecommendation(@RequestBody RecommendationRequest request) {
        try {
            RecommendationResponse response = learningLoopService.getLearningRecommendation(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    /**
     * Layer 3: Practice Quiz Generation - Sinh bộ câu hỏi luyện tập
     * POST /api/ai/generate-practice-quiz
     */
    @Operation(
            summary = "Layer 3: Practice Quiz Generation - Sinh bộ câu hỏi luyện tập",
            description = "API Sinh bộ câu hỏi luyện tập.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Thành công",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Assignment.class))
                    )
            }
    )
    @PostMapping("/generate-practice-quiz")
    public ResponseEntity<PracticeQuizResponse> generatePracticeQuiz(@RequestBody PracticeQuizRequest request) {
        try {
            PracticeQuizResponse response = learningLoopService.generatePracticeQuiz(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    /**
     * Layer 3.5: Review Practice Results - Xem lại kết quả bài luyện tập
     * POST /api/ai/review-practice-results
     */
    @Operation(
            summary = "Layer 3.5: Review Practice Results - Xem lại kết quả bài luyện tập",
            description = "API xem lại chi tiết kết quả bài luyện tập với feedback từng câu, giải thích lỗi sai và phân tích theo topic.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Thành công",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Assignment.class))
                    )
            }
    )
    @PostMapping("/review-practice-results")
    public ResponseEntity<ReviewPracticeResponse> reviewPracticeResults(@RequestBody ReviewPracticeRequest request) {
        try {
            ReviewPracticeResponse response = learningLoopService.reviewPracticeResults(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    /**
     * Layer 4: Improvement Evaluation - Đánh giá tiến bộ sau luyện tập
     * POST /api/ai/improvement-evaluation
     */
    @Operation(
            summary = "Layer 4: Improvement Evaluation - Đánh giá tiến bộ sau luyện tập",
            description = "API Đánh giá tiến bộ sau luyện tập.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Thành công",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Assignment.class))
                    )
            }
    )
    @PostMapping("/improvement-evaluation")
    public ResponseEntity<ImprovementResponse> evaluateImprovement(@RequestBody ImprovementRequest request) {
        try {
            ImprovementResponse response = learningLoopService.evaluateImprovement(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    /**
     * Full Learning Loop - Chạy toàn bộ Layer 1, 2, 3 cùng lúc
     * POST /api/ai/full-learning-loop
     */
    @Operation(
            summary = "Full Learning Loop - Chạy toàn bộ Layer 1, 2, 3 cùng lúc",
            description = "Chạy toàn bộ Layer 1, 2, 3 cùng lúc.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Thành công",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Assignment.class))
                    )
            }
    )
    @PostMapping("/full-learning-loop")
    public ResponseEntity<FullLearningLoopResponse> runFullLearningLoop(@RequestBody FeedbackRequest request) {
        try {
            FullLearningLoopResponse response = learningLoopService.runFullLearningLoop(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // ========= Student self-serve GET endpoints =========
    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof org.example.estudebackendspring.entity.User u) {
            return u.getUserId();
        }
        return null;
    }
    
    // ========= LAYER 1: FEEDBACK =========
    @Operation(
            summary = "Lấy TẤT CẢ Feedback layer 1 của user hiện tại (Bearer Token)",
            description = "Trả về danh sách tất cả kết quả feedback theo thứ tự mới nhất"
    )
    @GetMapping("/me/feedback")
    public ResponseEntity<?> getMyAllFeedback() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var results = aiAnalysisService.getAllResultsByStudentIdAndType(uid, AnalysisType.LEARNING_FEEDBACK);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "Lấy Feedback mới nhất layer 1 của user hiện tại (Bearer Token)",
            description = "Trả về kết quả feedback mới nhất"
    )
    @GetMapping("/me/feedback/latest")
    public ResponseEntity<?> getMyLatestFeedback() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var res = aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.LEARNING_FEEDBACK);
        return ResponseEntity.ok(res != null ? res : new AIAnalysisResult());
    }
    
    @Operation(
            summary = "Lấy TẤT CẢ Feedback layer 1 theo assignment_id của user hiện tại (Bearer Token)",
            description = "Trả về tất cả feedback của một assignment cụ thể"
    )
    @GetMapping("/me/feedback/assignment/{assignmentId}")
    public ResponseEntity<?> getMyFeedbackByAssignment(@PathVariable String assignmentId) {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var results = aiAnalysisService.getResultsByStudentAndAssignmentAndType(
                uid, assignmentId, AnalysisType.LEARNING_FEEDBACK);
        return ResponseEntity.ok(results);
    }
    
    // ========= LAYER 2: RECOMMENDATION =========
    @Operation(
            summary = "Lấy TẤT CẢ Recommendation layer 2 của user hiện tại (Bearer Token)",
            description = "Trả về danh sách tất cả recommendation theo thứ tự mới nhất"
    )
    @GetMapping("/me/recommendation")
    public ResponseEntity<?> getMyAllRecommendation() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var results = aiAnalysisService.getAllResultsByStudentIdAndType(uid, AnalysisType.LEARNING_RECOMMENDATION);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "Lấy Recommendation mới nhất layer 2 của user hiện tại (Bearer Token)",
            description = "Trả về recommendation mới nhất"
    )
    @GetMapping("/me/recommendation/latest")
    public ResponseEntity<?> getMyLatestRecommendation() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var res = aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.LEARNING_RECOMMENDATION);
        return ResponseEntity.ok(res != null ? res : new AIAnalysisResult());
    }
    
    @Operation(
            summary = "Lấy TẤT CẢ Recommendation layer 2 theo assignment_id của user hiện tại (Bearer Token)",
            description = "Trả về tất cả recommendation của một assignment cụ thể"
    )
    @GetMapping("/me/recommendation/assignment/{assignmentId}")
    public ResponseEntity<?> getMyRecommendationByAssignment(@PathVariable String assignmentId) {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var results = aiAnalysisService.getResultsByStudentAndAssignmentAndType(
                uid, assignmentId, AnalysisType.LEARNING_RECOMMENDATION);
        return ResponseEntity.ok(results);
    }
    
    // ========= LAYER 3: PRACTICE QUIZ =========
    @Operation(
            summary = "Lấy TẤT CẢ Practice Quiz layer 3 của user hiện tại (Bearer Token)",
            description = "Trả về danh sách tất cả practice quiz theo thứ tự mới nhất"
    )
    @GetMapping("/me/quiz")
    public ResponseEntity<?> getMyAllPracticeQuiz() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var results = aiAnalysisService.getAllResultsByStudentIdAndType(uid, AnalysisType.PRACTICE_QUIZ);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "Lấy Practice Quiz mới nhất layer 3 của user hiện tại (Bearer Token)",
            description = "Trả về practice quiz mới nhất"
    )
    @GetMapping("/me/quiz/latest")
    public ResponseEntity<?> getMyLatestPracticeQuiz() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var res = aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.PRACTICE_QUIZ);
        return ResponseEntity.ok(res != null ? res : new AIAnalysisResult());
    }
    
    @Operation(
            summary = "Lấy TẤT CẢ Practice Quiz layer 3 theo assignment_id của user hiện tại (Bearer Token)",
            description = "Trả về tất cả practice quiz của một assignment cụ thể"
    )
    @GetMapping("/me/quiz/assignment/{assignmentId}")
    public ResponseEntity<?> getMyPracticeQuizByAssignment(@PathVariable String assignmentId) {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var results = aiAnalysisService.getResultsByStudentAndAssignmentAndType(
                uid, assignmentId, AnalysisType.PRACTICE_QUIZ);
        return ResponseEntity.ok(results);
    }
    
    // ========= LAYER 3.5: PRACTICE REVIEW =========
    @Operation(
            summary = "Lấy TẤT CẢ Practice Review layer 3.5 của user hiện tại (Bearer Token)",
            description = "Trả về danh sách tất cả kết quả review bài luyện tập theo thứ tự mới nhất"
    )
    @GetMapping("/me/practice-review")
    public ResponseEntity<?> getMyAllPracticeReview() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var results = aiAnalysisService.getAllResultsByStudentIdAndType(uid, AnalysisType.PRACTICE_REVIEW);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "Lấy Practice Review mới nhất layer 3.5 của user hiện tại (Bearer Token)",
            description = "Trả về kết quả review bài luyện tập mới nhất"
    )
    @GetMapping("/me/practice-review/latest")
    public ResponseEntity<?> getMyLatestPracticeReview() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var res = aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.PRACTICE_REVIEW);
        return ResponseEntity.ok(res != null ? res : new AIAnalysisResult());
    }
    
    @Operation(
            summary = "Lấy TẤT CẢ Practice Review layer 3.5 theo assignment_id của user hiện tại (Bearer Token)",
            description = "Trả về tất cả kết quả review của một assignment cụ thể"
    )
    @GetMapping("/me/practice-review/assignment/{assignmentId}")
    public ResponseEntity<?> getMyPracticeReviewByAssignment(@PathVariable String assignmentId) {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var results = aiAnalysisService.getResultsByStudentAndAssignmentAndType(
                uid, assignmentId, AnalysisType.PRACTICE_REVIEW);
        return ResponseEntity.ok(results);
    }
    
    // ========= LAYER 4: IMPROVEMENT =========
    @Operation(
            summary = "Lấy TẤT CẢ Improvement layer 4 của user hiện tại (Bearer Token)",
            description = "Trả về danh sách tất cả improvement evaluation theo thứ tự mới nhất"
    )
    @GetMapping("/me/improvement")
    public ResponseEntity<?> getMyAllImprovement() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var results = aiAnalysisService.getAllResultsByStudentIdAndType(uid, AnalysisType.IMPROVEMENT_EVALUATION);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "Lấy Improvement mới nhất layer 4 của user hiện tại (Bearer Token)",
            description = "Trả về improvement evaluation mới nhất"
    )
    @GetMapping("/me/improvement/latest")
    public ResponseEntity<?> getMyLatestImprovement() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var res = aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.IMPROVEMENT_EVALUATION);
        return ResponseEntity.ok(res != null ? res : new AIAnalysisResult());
    }
    
    @Operation(
            summary = "Lấy TẤT CẢ Improvement layer 4 theo assignment_id của user hiện tại (Bearer Token)",
            description = "Trả về tất cả improvement evaluation của một assignment cụ thể"
    )
    @GetMapping("/me/improvement/assignment/{assignmentId}")
    public ResponseEntity<?> getMyImprovementByAssignment(@PathVariable String assignmentId) {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var results = aiAnalysisService.getResultsByStudentAndAssignmentAndType(
                uid, assignmentId, AnalysisType.IMPROVEMENT_EVALUATION);
        return ResponseEntity.ok(results);
    }
    
    // ========= FULL LEARNING LOOP =========
    @Operation(
            summary = "Lấy TẤT CẢ Full Learning Loop của user hiện tại (Bearer Token)",
            description = "Trả về danh sách tất cả full learning loop theo thứ tự mới nhất"
    )
    @GetMapping("/me/full-learning-loop")
    public ResponseEntity<?> getMyAllFullLearningLoop() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var results = aiAnalysisService.getAllResultsByStudentIdAndType(uid, AnalysisType.FULL_LEARNING_LOOP);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "Lấy Full Learning Loop mới nhất của user hiện tại (Bearer Token)",
            description = "Nếu FE dùng full learning loop thì dùng API này để lấy kết quả mới nhất"
    )
    @GetMapping("/me/full-learning-loop/latest")
    public ResponseEntity<?> getMyLatestFullLearningLoop() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var res = aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.FULL_LEARNING_LOOP);
        return ResponseEntity.ok(res != null ? res : new AIAnalysisResult());
    }
    
    @Operation(
            summary = "Lấy TẤT CẢ Full Learning Loop theo assignment_id của user hiện tại (Bearer Token)",
            description = "Trả về tất cả full learning loop của một assignment cụ thể"
    )
    @GetMapping("/me/full-learning-loop/assignment/{assignmentId}")
    public ResponseEntity<?> getMyFullLearningLoopByAssignment(@PathVariable String assignmentId) {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var results = aiAnalysisService.getResultsByStudentAndAssignmentAndType(
                uid, assignmentId, AnalysisType.FULL_LEARNING_LOOP);
        return ResponseEntity.ok(results);
    }

    // ========= DASHBOARD - COMBINED VIEW =========
    @Operation(
            summary = "Dashboard Learning Loop - Lấy kết quả mới nhất của tất cả layers (Bearer Token)",
            description = "Nếu FE chạy từng layer 1,2,3,3.5,4 thì dùng API này để lấy dashboard tổng hợp"
    )
    @GetMapping("/me/dashboard")
    public ResponseEntity<?> getMyLearningLoopDashboard() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");

        Map<String, Object> data = new HashMap<>();
        data.put("feedback", aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.LEARNING_FEEDBACK));
        data.put("recommendation", aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.LEARNING_RECOMMENDATION));
        data.put("practice_quiz", aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.PRACTICE_QUIZ));
        data.put("practice_review", aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.PRACTICE_REVIEW));  // Layer 3.5
        data.put("improvement", aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.IMPROVEMENT_EVALUATION));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data
        ));
    }
    
    // ========= ADMIN/TEACHER ENDPOINTS - Lấy theo student_id =========
    @Operation(
            summary = "[Admin/Teacher] Lấy TẤT CẢ Feedback layer 1 của một student",
            description = "Dành cho admin/teacher xem tất cả feedback của một học sinh"
    )
    @GetMapping("/student/{studentId}/feedback")
    public ResponseEntity<?> getStudentAllFeedback(@PathVariable Long studentId) {
        var results = aiAnalysisService.getAllResultsByStudentIdAndType(studentId, AnalysisType.LEARNING_FEEDBACK);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "[Admin/Teacher] Lấy Feedback layer 1 theo assignment của một student",
            description = "Dành cho admin/teacher xem feedback của một học sinh trong một assignment"
    )
    @GetMapping("/student/{studentId}/feedback/assignment/{assignmentId}")
    public ResponseEntity<?> getStudentFeedbackByAssignment(
            @PathVariable Long studentId, @PathVariable String assignmentId) {
        var results = aiAnalysisService.getResultsByStudentAndAssignmentAndType(
                studentId, assignmentId, AnalysisType.LEARNING_FEEDBACK);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "[Admin/Teacher] Lấy TẤT CẢ Recommendation layer 2 của một student",
            description = "Dành cho admin/teacher xem tất cả recommendation của một học sinh"
    )
    @GetMapping("/student/{studentId}/recommendation")
    public ResponseEntity<?> getStudentAllRecommendation(@PathVariable Long studentId) {
        var results = aiAnalysisService.getAllResultsByStudentIdAndType(studentId, AnalysisType.LEARNING_RECOMMENDATION);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "[Admin/Teacher] Lấy Recommendation layer 2 theo assignment của một student",
            description = "Dành cho admin/teacher xem recommendation của một học sinh trong một assignment"
    )
    @GetMapping("/student/{studentId}/recommendation/assignment/{assignmentId}")
    public ResponseEntity<?> getStudentRecommendationByAssignment(
            @PathVariable Long studentId, @PathVariable String assignmentId) {
        var results = aiAnalysisService.getResultsByStudentAndAssignmentAndType(
                studentId, assignmentId, AnalysisType.LEARNING_RECOMMENDATION);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "[Admin/Teacher] Lấy TẤT CẢ Practice Quiz layer 3 của một student",
            description = "Dành cho admin/teacher xem tất cả practice quiz của một học sinh"
    )
    @GetMapping("/student/{studentId}/quiz")
    public ResponseEntity<?> getStudentAllPracticeQuiz(@PathVariable Long studentId) {
        var results = aiAnalysisService.getAllResultsByStudentIdAndType(studentId, AnalysisType.PRACTICE_QUIZ);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "[Admin/Teacher] Lấy Practice Quiz layer 3 theo assignment của một student",
            description = "Dành cho admin/teacher xem practice quiz của một học sinh trong một assignment"
    )
    @GetMapping("/student/{studentId}/quiz/assignment/{assignmentId}")
    public ResponseEntity<?> getStudentPracticeQuizByAssignment(
            @PathVariable Long studentId, @PathVariable String assignmentId) {
        var results = aiAnalysisService.getResultsByStudentAndAssignmentAndType(
                studentId, assignmentId, AnalysisType.PRACTICE_QUIZ);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "[Admin/Teacher] Lấy TẤT CẢ Practice Review layer 3.5 của một student",
            description = "Dành cho admin/teacher xem tất cả kết quả review bài luyện tập của một học sinh"
    )
    @GetMapping("/student/{studentId}/practice-review")
    public ResponseEntity<?> getStudentAllPracticeReview(@PathVariable Long studentId) {
        var results = aiAnalysisService.getAllResultsByStudentIdAndType(studentId, AnalysisType.PRACTICE_REVIEW);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "[Admin/Teacher] Lấy Practice Review layer 3.5 theo assignment của một student",
            description = "Dành cho admin/teacher xem kết quả review của một học sinh trong một assignment"
    )
    @GetMapping("/student/{studentId}/practice-review/assignment/{assignmentId}")
    public ResponseEntity<?> getStudentPracticeReviewByAssignment(
            @PathVariable Long studentId, @PathVariable String assignmentId) {
        var results = aiAnalysisService.getResultsByStudentAndAssignmentAndType(
                studentId, assignmentId, AnalysisType.PRACTICE_REVIEW);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "[Admin/Teacher] Lấy TẤT CẢ Improvement layer 4 của một student",
            description = "Dành cho admin/teacher xem tất cả improvement evaluation của một học sinh"
    )
    @GetMapping("/student/{studentId}/improvement")
    public ResponseEntity<?> getStudentAllImprovement(@PathVariable Long studentId) {
        var results = aiAnalysisService.getAllResultsByStudentIdAndType(studentId, AnalysisType.IMPROVEMENT_EVALUATION);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "[Admin/Teacher] Lấy Improvement layer 4 theo assignment của một student",
            description = "Dành cho admin/teacher xem improvement evaluation của một học sinh trong một assignment"
    )
    @GetMapping("/student/{studentId}/improvement/assignment/{assignmentId}")
    public ResponseEntity<?> getStudentImprovementByAssignment(
            @PathVariable Long studentId, @PathVariable String assignmentId) {
        var results = aiAnalysisService.getResultsByStudentAndAssignmentAndType(
                studentId, assignmentId, AnalysisType.IMPROVEMENT_EVALUATION);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "[Admin/Teacher] Lấy TẤT CẢ Full Learning Loop của một student",
            description = "Dành cho admin/teacher xem tất cả full learning loop của một học sinh"
    )
    @GetMapping("/student/{studentId}/full-learning-loop")
    public ResponseEntity<?> getStudentAllFullLearningLoop(@PathVariable Long studentId) {
        var results = aiAnalysisService.getAllResultsByStudentIdAndType(studentId, AnalysisType.FULL_LEARNING_LOOP);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "[Admin/Teacher] Lấy Full Learning Loop theo assignment của một student",
            description = "Dành cho admin/teacher xem full learning loop của một học sinh trong một assignment"
    )
    @GetMapping("/student/{studentId}/full-learning-loop/assignment/{assignmentId}")
    public ResponseEntity<?> getStudentFullLearningLoopByAssignment(
            @PathVariable Long studentId, @PathVariable String assignmentId) {
        var results = aiAnalysisService.getResultsByStudentAndAssignmentAndType(
                studentId, assignmentId, AnalysisType.FULL_LEARNING_LOOP);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
            summary = "[Admin/Teacher] Dashboard Learning Loop của một student",
            description = "Dành cho admin/teacher xem dashboard tổng hợp của một học sinh"
    )
    @GetMapping("/student/{studentId}/dashboard")
    public ResponseEntity<?> getStudentLearningLoopDashboard(@PathVariable Long studentId) {
        Map<String, Object> data = new HashMap<>();
        data.put("feedback", aiAnalysisService.getLatestResultByStudentId(studentId, AnalysisType.LEARNING_FEEDBACK));
        data.put("recommendation", aiAnalysisService.getLatestResultByStudentId(studentId, AnalysisType.LEARNING_RECOMMENDATION));
        data.put("practice_quiz", aiAnalysisService.getLatestResultByStudentId(studentId, AnalysisType.PRACTICE_QUIZ));
        data.put("practice_review", aiAnalysisService.getLatestResultByStudentId(studentId, AnalysisType.PRACTICE_REVIEW));  // Layer 3.5
        data.put("improvement", aiAnalysisService.getLatestResultByStudentId(studentId, AnalysisType.IMPROVEMENT_EVALUATION));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data
        ));
    }
}
