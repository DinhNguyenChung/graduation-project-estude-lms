package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.ApiResponse;
import org.example.estudebackendspring.dto.AuthResponse;
import org.example.estudebackendspring.entity.Question;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.enums.ActionType;
import org.example.estudebackendspring.repository.UserRepository;
import org.example.estudebackendspring.service.LogEntryService;
import org.example.estudebackendspring.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class QuestionController {
    private final QuestionService questionService;
    private final LogEntryService logEntryService;
    private final UserRepository userRepository;

    public QuestionController(QuestionService questionService, LogEntryService logEntryService, UserRepository userRepository) {
        this.questionService = questionService;
        this.logEntryService = logEntryService;
        this.userRepository = userRepository;
    }
    // POST /api/assignments/{assignmentId}/questions
    @PostMapping("/questions/assignments/{assignmentId}")
    public ResponseEntity<?> addQuestion(@PathVariable Long assignmentId, @RequestBody Question question) {
        try {
            Question saved = questionService.addQuestion(assignmentId, question);
            User user = userRepository.findById(saved.getAssignment().getTeacher().getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid actingUserId"));
            // Log question creation
            try {
                logEntryService.createLog(
                    "Question",
                    saved.getQuestionId(),
                    "Thêm câu hỏi mới vào bài tập: " + (saved.getQuestionText() != null ? 
                        saved.getQuestionText().substring(0, Math.min(50, saved.getQuestionText().length())) + "..." : "Unknown"),
                    ActionType.CREATE,
                    assignmentId,
                    "Assignment",
                    user
                );
            } catch (Exception e) {
                System.err.println("Failed to log question creation: " + e.getMessage());
            }
            
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
            User user = userRepository.findById(saved.getAssignment().getTeacher().getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid actingUserId"));
            // Log question update
            try {
                logEntryService.createLog(
                    "Question",
                    questionId,
                    "Cập nhật câu hỏi: " + (saved.getQuestionText() != null ? 
                        saved.getQuestionText().substring(0, Math.min(50, saved.getQuestionText().length())) + "..." : "Unknown"),
                    ActionType.UPDATE,
                    saved.getAssignment() != null ? saved.getAssignment().getAssignmentId() : null,
                    "Assignment",
                    user
                );
            } catch (Exception e) {
                System.err.println("Failed to log question update: " + e.getMessage());
            }
            
            return ResponseEntity.ok(new AuthResponse(true, "Question updated successfully", saved));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
    // DELETE /api/questions/{questionId}
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        try {
            Question question = questionService.getQuestion(questionId);
            User user = userRepository.findById(question.getAssignment().getTeacher().getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid actingUserId"));
            // Log question deletion
            try {
                logEntryService.createLog(
                    "Question",
                    questionId,
                    "Xóa câu hỏi khỏi bài tập",
                    ActionType.DELETE,
                    question.getAssignment().getAssignmentId(),
                    "Assignment",
                    user
                );
            } catch (Exception e) {
                System.err.println("Failed to log question deletion: " + e.getMessage());
            }
            
            questionService.deleteQuestion(questionId);
            return ResponseEntity.ok(new ApiResponse(true, "Question deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new ApiResponse(false, e.getMessage()));
        }
    }

}
