package org.example.estudebackendspring.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.analytics.QuestionBankStatisticsDTO;
import org.example.estudebackendspring.dto.analytics.QuestionUsageStatsDTO;
import org.example.estudebackendspring.service.LogEntryService;
import org.example.estudebackendspring.service.QuestionAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Admin Analytics Dashboard
 * Provides question bank statistics and insights
 */
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {
    
    private final QuestionAnalyticsService questionAnalyticsService;
    private final LogEntryService logEntryService;
    
    /**
     * GET /api/admin/analytics/questions/overview
     * Get overall statistics of question bank
     * 
     * Response:
     * {
     *   "total_questions": 1250,
     *   "active_questions": 1180,
     *   "inactive_questions": 70,
     *   "by_difficulty": {
     *     "EASY": 450,
     *     "MEDIUM": 520,
     *     "HARD": 200,
     *     "EXPERT": 80
     *   },
     *   "by_subject": {
     *     "Toán": 350,
     *     "Vật Lý": 280,
     *     ...
     *   },
     *   "by_topic": {
     *     "Đại Số": 120,
     *     "Hình Học": 90,
     *     ...
     *   }
     * }
     */
    @GetMapping("/questions/overview")
    public ResponseEntity<QuestionBankStatisticsDTO> getQuestionBankOverview() {
        try {
            log.info("Admin requesting question bank overview statistics");
            
            QuestionBankStatisticsDTO stats = questionAnalyticsService.getQuestionBankOverview();
            
//            logEntryService.createLogEntry(
//                    "ANALYTICS",
//                    "Admin viewed question bank overview",
//                    "SUCCESS"
//            );
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error fetching question bank overview: {}", e.getMessage(), e);
            
//            logEntryService.createLogEntry(
//                    "ANALYTICS",
//                    "Error fetching question bank overview: " + e.getMessage(),
//                    "ERROR"
//            );
            
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/admin/analytics/questions/{questionId}/stats
     * Get detailed usage statistics for a specific question
     * 
     * Response:
     * {
     *   "question_id": 123,
     *   "question_text": "Giải phương trình...",
     *   "topic": "Phương trình bậc 2",
     *   "difficulty": "MEDIUM",
     *   "usage_stats": {
     *     "times_used": 45,
     *     "total_attempts": 380,
     *     "correct_attempts": 245,
     *     "incorrect_attempts": 135,
     *     "average_accuracy": 64.5,
     *     "average_time_seconds": 180
     *   },
     *   "common_mistakes": [
     *     {
     *       "incorrect_answer": "x = 3",
     *       "count": 78,
     *       "percentage": 57.7,
     *       "reason": "Quên kiểm tra điều kiện..."
     *     }
     *   ]
     * }
     */
    @GetMapping("/questions/{questionId}/stats")
    public ResponseEntity<QuestionUsageStatsDTO> getQuestionUsageStats(
            @PathVariable Long questionId) {
        try {
            log.info("Admin requesting usage stats for question ID: {}", questionId);
            
            QuestionUsageStatsDTO stats = questionAnalyticsService.getQuestionUsageStats(questionId);
            
//            logEntryService.createLogEntry(
//                    "ANALYTICS",
//                    "Admin viewed question stats for ID: " + questionId,
//                    "SUCCESS"
//            );
            
            return ResponseEntity.ok(stats);
            
        } catch (RuntimeException e) {
            log.error("Error fetching question stats for ID {}: {}", questionId, e.getMessage());
            
//            logEntryService.createLogEntry(
//                    "ANALYTICS",
//                    "Question not found with ID: " + questionId,
//                    "ERROR"
//            );
            
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Internal error fetching question stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/admin/analytics/questions/usage-ranking
     * Get top N questions by usage frequency
     * 
     * Query params:
     * - limit: Number of questions to return (default: 20)
     * 
     * Response:
     * [
     *   {
     *     "question_id": 456,
     *     "question_text": "...",
     *     "usage_stats": { ... }
     *   },
     *   ...
     * ]
     */
    @GetMapping("/questions/usage-ranking")
    public ResponseEntity<List<QuestionUsageStatsDTO>> getQuestionUsageRanking(
            @RequestParam(defaultValue = "20") int limit) {
        try {
            log.info("Admin requesting top {} questions by usage", limit);
            
            if (limit <= 0 || limit > 100) {
                log.warn("Invalid limit value: {}. Using default 20.", limit);
                limit = 20;
            }
            
            List<QuestionUsageStatsDTO> ranking = questionAnalyticsService.getQuestionUsageRanking(limit);
            
//            logEntryService.createLogEntry(
//                    "ANALYTICS",
//                    "Admin viewed question usage ranking (limit: " + limit + ")",
//                    "SUCCESS"
//            );
            
            return ResponseEntity.ok(ranking);
            
        } catch (Exception e) {
            log.error("Error fetching question usage ranking: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/admin/analytics/questions/needs-improvement
     * Get list of questions that need review (low accuracy, high error rate)
     * 
     * Response:
     * [
     *   {
     *     "question_id": 789,
     *     "question_text": "...",
     *     "usage_stats": {
     *       "average_accuracy": 32.5,
     *       ...
     *     }
     *   },
     *   ...
     * ]
     */
    @GetMapping("/questions/needs-improvement")
    public ResponseEntity<List<QuestionUsageStatsDTO>> getQuestionsNeedingImprovement() {
        try {
            log.info("Admin requesting questions that need improvement");
            
            List<QuestionUsageStatsDTO> questions = questionAnalyticsService.getQuestionsNeedingImprovement();
            
//            logEntryService.createLogEntry(
//                    "ANALYTICS",
//                    "Admin viewed questions needing improvement",
//                    "SUCCESS"
//            );
            
            return ResponseEntity.ok(questions);
            
        } catch (Exception e) {
            log.error("Error fetching questions needing improvement: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
