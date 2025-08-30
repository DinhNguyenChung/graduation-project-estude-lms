package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.ApiResponse;
import org.example.estudebackendspring.dto.AuthResponse;
import org.example.estudebackendspring.entity.Question;
import org.example.estudebackendspring.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class QuestionController {
    private final QuestionService questionService;
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }
    // POST /api/assignments/{assignmentId}/questions
    @PostMapping("/assignments/{assignmentId}/questions")
    public ResponseEntity<?> addQuestion(@PathVariable Long assignmentId, @RequestBody Question question) {
        try {
            Question saved = questionService.addQuestion(assignmentId, question);
            return ResponseEntity.ok(new AuthResponse(true, "Question added successfully", saved));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }

    // PUT /api/questions/{questionId}
    @PutMapping("/questions/{questionId}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long questionId, @RequestBody Question updated) {
        try {
            Question saved = questionService.updateQuestion(questionId, updated);
            return ResponseEntity.ok(new AuthResponse(true, "Question updated successfully", saved));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
    // DELETE /api/questions/{questionId}
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        try {
            questionService.deleteQuestion(questionId);
            return ResponseEntity.ok(new ApiResponse(true, "Question deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new ApiResponse(false, e.getMessage()));
        }
    }

}
