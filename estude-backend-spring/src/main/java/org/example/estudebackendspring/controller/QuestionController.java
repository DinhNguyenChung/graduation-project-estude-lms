package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.ApiResponse;
import org.example.estudebackendspring.dto.AuthResponse;
import org.example.estudebackendspring.dto.QuestionBankRequest;
import org.example.estudebackendspring.entity.Question;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.enums.ActionType;
import org.example.estudebackendspring.enums.DifficultyLevel;
import org.example.estudebackendspring.repository.UserRepository;
import org.example.estudebackendspring.service.LogEntryService;
import org.example.estudebackendspring.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    
    // ========== QUESTION BANK ENDPOINTS ==========
    
    /**
     * Tạo câu hỏi mới cho question bank
     * POST /api/questions/bank
     */
    @PostMapping("/questions/bank")
    public ResponseEntity<?> createQuestionBank(@RequestBody QuestionBankRequest request) {
        try {
            Question saved = questionService.createQuestionBank(request);
            return ResponseEntity.ok(new AuthResponse(true, "Question added to bank successfully", saved));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
    
    /**
     * Lấy tất cả câu hỏi trong question bank
     * GET /api/questions/bank
     */
    @GetMapping("/questions/bank")
    public ResponseEntity<?> getAllQuestionBank() {
        try {
            List<Question> questions = questionService.getAllQuestionBank();
            return ResponseEntity.ok(new AuthResponse(true, "Questions retrieved successfully", questions));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
    
    /**
     * Lấy câu hỏi trong question bank theo topic
     * GET /api/questions/bank/topic/{topicId}
     * Có thể filter theo difficulty: ?difficulty=EASY
     */
    @GetMapping("/questions/bank/topic/{topicId}")
    public ResponseEntity<?> getQuestionBankByTopic(
            @PathVariable Long topicId,
            @RequestParam(required = false) String difficulty) {
        try {
            List<Question> questions;
            
            if (difficulty != null && !difficulty.isEmpty()) {
                // Lọc theo topic và độ khó
                DifficultyLevel difficultyLevel = DifficultyLevel.valueOf(difficulty);
                questions = questionService.getQuestionBankByTopicAndDifficulty(topicId, difficultyLevel);
            } else {
                // Chỉ lọc theo topic
                questions = questionService.getQuestionBankByTopic(topicId);
            }
            
            return ResponseEntity.ok(new AuthResponse(true, "Questions retrieved successfully", questions));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new AuthResponse(false, "Invalid difficulty level", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
    
    /**
     * Lấy một câu hỏi từ question bank theo ID
     * GET /api/questions/bank/{questionId}
     */
    @GetMapping("/questions/bank/{questionId}")
    public ResponseEntity<?> getQuestionBankById(@PathVariable Long questionId) {
        try {
            Question question = questionService.getQuestion(questionId);
            
            // Validate là question bank
            if (question.getIsQuestionBank() == null || !question.getIsQuestionBank()) {
                return ResponseEntity.ok(new AuthResponse(false, "This question is not a question bank question", null));
            }
            
            return ResponseEntity.ok(new AuthResponse(true, "Question retrieved successfully", question));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
    
    /**
     * Cập nhật câu hỏi trong question bank
     * PUT /api/questions/bank/{questionId}
     */
    @PutMapping("/questions/bank/{questionId}")
    public ResponseEntity<?> updateQuestionBank(
            @PathVariable Long questionId,
            @RequestBody QuestionBankRequest request) {
        try {
            Question updated = questionService.updateQuestionBank(questionId, request);
            return ResponseEntity.ok(new AuthResponse(true, "Question updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
    
    /**
     * Xóa câu hỏi từ question bank
     * DELETE /api/questions/bank/{questionId}
     */
    @DeleteMapping("/questions/bank/{questionId}")
    public ResponseEntity<?> deleteQuestionBank(@PathVariable Long questionId) {
        try {
            questionService.deleteQuestionBank(questionId);
            return ResponseEntity.ok(new ApiResponse(true, "Question deleted from bank successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new ApiResponse(false, e.getMessage()));
        }
    }
    
    /**
     * Đếm số câu hỏi trong question bank của một topic
     * GET /api/questions/bank/topic/{topicId}/count
     */
    @GetMapping("/questions/bank/topic/{topicId}/count")
    public ResponseEntity<?> countQuestionBankByTopic(@PathVariable Long topicId) {
        try {
            Long count = questionService.countQuestionBankByTopic(topicId);
            return ResponseEntity.ok(new AuthResponse(true, "Count retrieved successfully", count));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
    
    // ========== ASSIGNMENT QUESTION ENDPOINTS ==========
    
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
