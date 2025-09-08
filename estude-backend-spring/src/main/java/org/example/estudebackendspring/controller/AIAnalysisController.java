package org.example.estudebackendspring.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.enums.AnalysisType;
import org.example.estudebackendspring.service.AIAnalysisService;
import org.example.estudebackendspring.service.SubjectAnalysisService;
import org.example.estudebackendspring.service.SubmissionReportService;
import org.example.estudebackendspring.service.TestAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIAnalysisController {
    private final AIAnalysisService aiAnalysisService;
    private final SubjectAnalysisService subjectAnalysisService;
    private final SubmissionReportService submissionReportService;
    private final TestAnalysisService testAnalysisService;


    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Spring Boot API");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    public static record PredictReq(Long studentId) {}
    // Dự đoán học kỳ
    @PostMapping("/predict-semeter")
    public ResponseEntity<?> analyzePredict(@RequestBody PredictReq body) {
        if (body == null || body.studentId() == null) {
            return ResponseEntity.badRequest().body("studentId is required");
        }
        var result = aiAnalysisService.analyzePredict(body.studentId());
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
            return ResponseEntity.notFound().build();
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

}
