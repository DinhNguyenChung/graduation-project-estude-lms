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
     * Lấy tất cả câu hỏi trong question bank với pagination - OPTIMIZED
     * GET /api/questions/bank
     * 
     * Query params:
     * - page: số trang (mặc định: 0)
     * - size: số items per page (mặc định: 20)
     * - sortBy: trường để sort (mặc định: questionId)
     * - direction: ASC hoặc DESC (mặc định: DESC)
     * - full: true để lấy full details, false để lấy summary (mặc định: false)
     * 
     * Example: /api/questions/bank?page=0&size=20&sortBy=questionId&direction=DESC&full=false
     */
    @GetMapping("/questions/bank")
    public ResponseEntity<?> getAllQuestionBank(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "questionId") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(defaultValue = "false") boolean full) {
        try {
            // Validate pagination parameters
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 20; // max 100 items per page
            
            Object result;
            if (full) {
                // Full details - tốn resources hơn
                result = questionService.getAllQuestionBankFull(page, size);
            } else {
                // Summary - tối ưu cho listing
                result = questionService.getAllQuestionBankSummary(page, size, sortBy, direction);
            }
            
            return ResponseEntity.ok(new AuthResponse(true, "Questions retrieved successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
    
    /**
     * Lấy câu hỏi trong question bank theo topic với pagination - OPTIMIZED
     * GET /api/questions/bank/topic/{topicId}
     * 
     * Query params:
     * - difficulty: filter theo độ khó (EASY, MEDIUM, HARD)
     * - page: số trang (mặc định: 0)
     * - size: số items per page (mặc định: 20)
     * 
     * Example: /api/questions/bank/topic/1?difficulty=EASY&page=0&size=20
     */
    @GetMapping("/questions/bank/topic/{topicId}")
    public ResponseEntity<?> getQuestionBankByTopic(
            @PathVariable Long topicId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // Validate pagination parameters
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 20;
            
            Object result;
            if (difficulty != null && !difficulty.isEmpty()) {
                // Lọc theo topic và độ khó
                DifficultyLevel difficultyLevel = DifficultyLevel.valueOf(difficulty.toUpperCase());
                result = questionService.getQuestionBankByTopicAndDifficulty(topicId, difficultyLevel, page, size);
            } else {
                // Chỉ lọc theo topic
                result = questionService.getQuestionBankByTopicSummary(topicId, page, size);
            }
            
            return ResponseEntity.ok(new AuthResponse(true, "Questions retrieved successfully", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new AuthResponse(false, "Invalid difficulty level. Use: EASY, MEDIUM, HARD", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
    
    /**
     * Lấy một câu hỏi từ question bank theo ID - OPTIMIZED
     * GET /api/questions/bank/{questionId}
     * Trả về DTO với eager loaded relationships
     */
    @GetMapping("/questions/bank/{questionId}")
    public ResponseEntity<?> getQuestionBankById(@PathVariable Long questionId) {
        try {
            // Sử dụng optimized method với DTO
            Object result = questionService.getQuestionBankDetail(questionId);
            return ResponseEntity.ok(new AuthResponse(true, "Question retrieved successfully", result));
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
    
    // ========== FILTER BY SUBJECT AND GRADE ENDPOINTS ==========
    
    /**
     * Lấy câu hỏi trong question bank theo subject - OPTIMIZED
     * GET /api/questions/bank/subject/{subjectId}
     * 
     * Query params:
     * - page: số trang (mặc định: 0)
     * - size: số items per page (mặc định: 20)
     * - topicId: filter theo topic (optional)
     * 
     * Example: /api/questions/bank/subject/1?page=0&size=20&topicId=1
     */
    @GetMapping("/questions/bank/subject/{subjectId}")
    public ResponseEntity<?> getQuestionBankBySubject(
            @PathVariable Long subjectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long topicId) {
        try {
            // Validate pagination parameters
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 20;
            
            Object result = questionService.getQuestionBankBySubjectSummary(subjectId, topicId, page, size);
            return ResponseEntity.ok(new AuthResponse(true, "Questions retrieved successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
    
    /**
     * Lấy câu hỏi trong question bank theo subject và grade level - OPTIMIZED
     * GET /api/questions/bank/subject/{subjectId}/grade/{gradeLevel}
     * 
     * Query params:
     * - page: số trang (mặc định: 0)
     * - size: số items per page (mặc định: 20)
     * 
     * Grade level: GRADE_10, GRADE_11, GRADE_12, etc.
     * Example: /api/questions/bank/subject/1/grade/GRADE_10?page=0&size=20
     */
    @GetMapping("/questions/bank/subject/{subjectId}/grade/{gradeLevel}")
    public ResponseEntity<?> getQuestionBankBySubjectAndGradeLevel(
            @PathVariable Long subjectId,
            @PathVariable String gradeLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // Validate pagination parameters
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 20;
            
            // Parse GradeLevel enum
            org.example.estudebackendspring.enums.GradeLevel gradeLevelEnum = 
                org.example.estudebackendspring.enums.GradeLevel.valueOf(gradeLevel.toUpperCase());
            
            Object result = questionService.getQuestionBankBySubjectAndGradeLevelSummary(subjectId, gradeLevelEnum, page, size);
            return ResponseEntity.ok(new AuthResponse(true, "Questions retrieved successfully", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new AuthResponse(false, "Invalid grade level. Use: GRADE_10, GRADE_11, GRADE_12, etc.", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
    
    /**
     * Lấy câu hỏi trong question bank theo grade level - OPTIMIZED
     * GET /api/questions/bank/grade/{gradeLevel}
     * 
     * Query params:
     * - page: số trang (mặc định: 0)
     * - size: số items per page (mặc định: 20)
     * 
     * Grade level: GRADE_10, GRADE_11, GRADE_12, etc.
     * Example: /api/questions/bank/grade/GRADE_10?page=0&size=20
     */
    @GetMapping("/questions/bank/grade/{gradeLevel}")
    public ResponseEntity<?> getQuestionBankByGradeLevel(
            @PathVariable String gradeLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // Validate pagination parameters
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 20;
            
            // Parse GradeLevel enum
            org.example.estudebackendspring.enums.GradeLevel gradeLevelEnum = 
                org.example.estudebackendspring.enums.GradeLevel.valueOf(gradeLevel.toUpperCase());
            
            Object result = questionService.getQuestionBankByGradeLevelSummary(gradeLevelEnum, page, size);
            return ResponseEntity.ok(new AuthResponse(true, "Questions retrieved successfully", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new AuthResponse(false, "Invalid grade level. Use: GRADE_10, GRADE_11, GRADE_12, etc.", null));
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
