package org.example.estudebackendspring.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Test Utility Helper - Cung cấp các hàm tiện ích cho testing
 */
@Component
public class TestUtilHelper {
    
    private final ObjectMapper objectMapper;
    
    public TestUtilHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Tạo JSON string từ Object
     */
    public String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
    
    /**
     * Parse JSON string thành Object
     */
    public <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
    
    /**
     * Tạo test payload cho login
     */
    public Map<String, String> createLoginPayload(String username, String password) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("password", password);
        return payload;
    }
    
    /**
     * Tạo test payload cho tạo câu hỏi
     */
    public Map<String, Object> createQuestionPayload(String content, String topic, 
                                                     String difficulty, String answer) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", content);
        payload.put("topic", topic);
        payload.put("difficulty", difficulty);
        payload.put("correctAnswer", answer);
        payload.put("explanation", "Giải thích chi tiết");
        return payload;
    }
    
    /**
     * Tạo test payload cho tạo bài kiểm tra
     */
    public Map<String, Object> createTestPayload(String title, Long classId, 
                                                 Long subjectId, int duration) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("classId", classId);
        payload.put("subjectId", subjectId);
        payload.put("duration", duration);
        payload.put("totalQuestions", 10);
        payload.put("status", "PUBLISHED");
        return payload;
    }
    
    /**
     * Tạo test payload cho nộp bài thi
     */
    public Map<String, Object> createSubmissionPayload(Long testId, Map<String, String> answers) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("testId", testId);
        payload.put("answers", answers);
        return payload;
    }
    
    /**
     * Tạo test payload cho điểm danh
     */
    public Map<String, String> createAttendancePayload(Long studentId, String status, String reason) {
        Map<String, String> payload = new HashMap<>();
        payload.put("studentId", studentId.toString());
        payload.put("attendanceDate", "2025-11-29");
        payload.put("status", status);
        payload.put("reason", reason);
        return payload;
    }
    
    /**
     * Tạo test payload cho AI analysis
     */
    public Map<String, Object> createAIAnalysisPayload(Long submissionId, String analysisType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("submissionId", submissionId);
        payload.put("analysisType", analysisType);
        return payload;
    }
    
    /**
     * Validate token format
     */
    public boolean isValidJWTToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String[] parts = token.split("\\.");
        return parts.length == 3;
    }
    
    /**
     * Get current timestamp
     */
    public long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * Generate unique ID
     */
    public String generateUniqueId(String prefix) {
        return prefix + "_" + getCurrentTimestamp();
    }
}
