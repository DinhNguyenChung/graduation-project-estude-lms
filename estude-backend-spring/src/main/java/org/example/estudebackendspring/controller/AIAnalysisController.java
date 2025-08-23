package org.example.estudebackendspring.controller;

import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.service.AIAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIAnalysisController {
    private final AIAnalysisService aiAnalysisService;
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Spring Boot API");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    public static record PredictReq(Long studentId) {}

    @PostMapping("/predict")
    public ResponseEntity<?> analyzePredict(@RequestBody PredictReq body) {
        if (body == null || body.studentId() == null) {
            return ResponseEntity.badRequest().body("studentId is required");
        }
        var result = aiAnalysisService.analyzePredict(body.studentId());
        return ResponseEntity.ok(result);
    }
}
