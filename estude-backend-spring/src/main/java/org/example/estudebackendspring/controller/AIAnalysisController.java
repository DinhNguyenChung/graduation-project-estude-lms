package org.example.estudebackendspring.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.service.AIAnalysisService;
import org.example.estudebackendspring.service.SubjectAnalysisService;
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


    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Spring Boot API");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    public static record PredictReq(Long studentId) {}

    @PostMapping("/predict-semeter")
    public ResponseEntity<?> analyzePredict(@RequestBody PredictReq body) {
        if (body == null || body.studentId() == null) {
            return ResponseEntity.badRequest().body("studentId is required");
        }
        var result = aiAnalysisService.analyzePredict(body.studentId());
        return ResponseEntity.ok(result);
    }
    @GetMapping("/latest/{studentId}")
    public ResponseEntity<AIAnalysisResult> getLatestResult(@PathVariable Long studentId) {
        AIAnalysisResult result = aiAnalysisService.getLatestResultByStudentId(studentId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
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

}
