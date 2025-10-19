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
    @Operation(
            summary = "Lấy danhh sách LatestFeedback layer 1 ( Beer Token)",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Thành công",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Assignment.class))
                    )
            }
    )
    @GetMapping("/me/feedback/latest")
    public ResponseEntity<?> getMyLatestFeedback() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var res = aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.LEARNING_FEEDBACK);
        return ResponseEntity.ok(res != null ? res : new AIAnalysisResult());
    }
    @Operation(
            summary = "Lấy danhh sách LatestRecommendation layer 2 ( Beer Token)",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Thành công",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Assignment.class))
                    )
            }
    )
    @GetMapping("/me/recommendation/latest")
    public ResponseEntity<?> getMyLatestRecommendation() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var res = aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.LEARNING_RECOMMENDATION);
        return ResponseEntity.ok(res != null ? res : new AIAnalysisResult());
    }
    @Operation(
            summary = "Lấy danhh sách LatestPracticeQuiz layer 3 ( Beer Token)",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Thành công",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Assignment.class))
                    )
            }
    )
    @GetMapping("/me/quiz/latest")
    public ResponseEntity<?> getMyLatestPracticeQuiz() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var res = aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.PRACTICE_QUIZ);
        return ResponseEntity.ok(res != null ? res : new AIAnalysisResult());
    }
    @Operation(
            summary = "Lấy danhh sách LatestImprovement layer 4 ( Beer Token)",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Thành công",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Assignment.class))
                    )
            }
    )
    @GetMapping("/me/improvement/latest")
    public ResponseEntity<?> getMyLatestImprovement() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var res = aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.IMPROVEMENT_EVALUATION);
        return ResponseEntity.ok(res != null ? res : new AIAnalysisResult());
    }
    @Operation(
            summary = "Lấy danhh sách LatestFullLearningLoop ( Beer Token) ",
            description = "Nếu FE dùng layer Full learning Loop thì dùng API này để lấy danh sách ",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Thành công",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Assignment.class))
                    )
            }
    )
    @GetMapping("/me/full-learning-loop/latest")
    public ResponseEntity<?> getMyLatestFullLearningLoop() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");
        var res = aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.FULL_LEARNING_LOOP);
        return ResponseEntity.ok(res != null ? res : new AIAnalysisResult());
    }

    @Operation(
            summary = "Lấy danhh sách LearningLoopDashboard layer 1,2,3,4 ( Beer Token)",
            description = "Nếu FE chạy từng layer 1,2,3 thì dùng API này để lấy danh sách từng layer",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Thành công",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Assignment.class))
                    )
            }
    )
    @GetMapping("/me/dashboard")
    public ResponseEntity<?> getMyLearningLoopDashboard() {
        Long uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).body("Unauthorized");

        Map<String, Object> data = new HashMap<>();
        data.put("feedback", aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.LEARNING_FEEDBACK));
        data.put("recommendation", aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.LEARNING_RECOMMENDATION));
        data.put("practice_quiz", aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.PRACTICE_QUIZ));
        data.put("improvement", aiAnalysisService.getLatestResultByStudentId(uid, AnalysisType.IMPROVEMENT_EVALUATION));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data
        ));
    }
}
