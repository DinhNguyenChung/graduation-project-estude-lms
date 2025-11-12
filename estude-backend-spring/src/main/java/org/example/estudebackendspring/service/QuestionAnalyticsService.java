package org.example.estudebackendspring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.analytics.QuestionBankStatisticsDTO;
import org.example.estudebackendspring.dto.analytics.QuestionUsageStatsDTO;
import org.example.estudebackendspring.entity.Question;
import org.example.estudebackendspring.enums.DifficultyLevel;
import org.example.estudebackendspring.repository.QuestionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Question Bank Analytics
 * Provides statistical insights for Admin dashboard
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionAnalyticsService {
    
    private final QuestionRepository questionRepository;
    
    /**
     * Get overview statistics of entire question bank
     * Cached for 1 hour since question bank changes infrequently
     * 
     * ✅ OPTIMIZED: Uses native queries to avoid N+1 query problem
     * Instead of loading all Question entities with their options (causing hundreds of queries),
     * we use efficient COUNT queries directly on the database.
     */
    @Cacheable(value = "questionBankStats", unless = "#result == null")
    public QuestionBankStatisticsDTO getQuestionBankOverview() {
        log.info("Fetching question bank overview statistics using optimized native queries");
        
        // 1. Đếm tổng số câu hỏi (1 query)
        Long totalQuestions = questionRepository.countByIsQuestionBankTrue();
        
        // 2. Đếm theo difficulty level (1 query)
        Map<String, Integer> byDifficulty = questionRepository.countByDifficultyLevel().stream()
                .collect(Collectors.toMap(
                        row -> row[0] != null ? row[0].toString() : "UNKNOWN",
                        row -> ((Number) row[1]).intValue()
                ));
        
        // 3. Đếm theo subject (1 query)
        Map<String, Integer> bySubject = questionRepository.countBySubject().stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> ((Number) row[1]).intValue()
                ));
        
        // 4. Đếm theo topic (1 query)
        Map<String, Integer> byTopic = questionRepository.countByTopic().stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> ((Number) row[1]).intValue()
                ));
        
        // Giả sử tất cả câu hỏi trong question bank đều là active
        // (vì chúng đã được validate khi thêm vào question bank)
        int activeQuestions = totalQuestions != null ? totalQuestions.intValue() : 0;
        int inactiveQuestions = 0;
        
        log.info("✅ Question bank overview completed: total={}, byDifficulty={} groups, bySubject={} groups, byTopic={} groups", 
                totalQuestions, byDifficulty.size(), bySubject.size(), byTopic.size());
        
        return QuestionBankStatisticsDTO.builder()
                .totalQuestions(activeQuestions)
                .activeQuestions(activeQuestions)
                .inactiveQuestions(inactiveQuestions)
                .byDifficulty(byDifficulty)
                .bySubject(bySubject)
                .byTopic(byTopic)
                .build();
    }
    
    /**
     * Get detailed usage statistics for a specific question
     * @param questionId ID of the question
     * @return Usage statistics including accuracy, common mistakes
     */
    public QuestionUsageStatsDTO getQuestionUsageStats(Long questionId) {
        log.info("Fetching usage statistics for question ID: {}", questionId);
        
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + questionId));
        
        // TODO: Implement actual calculation from submission_answers table
        // For now, return mock data structure
        
        QuestionUsageStatsDTO.UsageStats usageStats = QuestionUsageStatsDTO.UsageStats.builder()
                .timesUsed(0)
                .totalAttempts(0)
                .correctAttempts(0)
                .incorrectAttempts(0)
                .averageAccuracy(0.0)
                .averageTimeSeconds((double) 0)
                .build();
        
        return QuestionUsageStatsDTO.builder()
                .questionId(question.getQuestionId())
                .questionText(question.getQuestionText())
                .topic(question.getTopic() != null ? question.getTopic().getName() : "N/A")
                .difficulty(question.getDifficultyLevel() != null ? question.getDifficultyLevel().name() : "UNKNOWN")
                .usageStats(usageStats)
                .commonMistakes(new ArrayList<>())
                .build();
    }
    
    /**
     * Get ranking of most frequently used questions
     * @param limit Number of top questions to return
     * @return List of questions sorted by usage frequency
     */
    @Cacheable(value = "questionUsageRanking", key = "#limit", unless = "#result == null || #result.isEmpty()")
    public List<QuestionUsageStatsDTO> getQuestionUsageRanking(int limit) {
        log.info("Fetching top {} questions by usage frequency", limit);
        
        // TODO: Implement actual ranking query from submission_answers
        // SELECT question_id, COUNT(*) as usage_count 
        // FROM submission_answers 
        // GROUP BY question_id 
        // ORDER BY usage_count DESC 
        // LIMIT :limit
        
        return new ArrayList<>();
    }
    
    /**
     * Identify questions that need improvement
     * Criteria: Low accuracy rate, high incorrect answer rate
     * @return List of questions that should be reviewed
     */
    public List<QuestionUsageStatsDTO> getQuestionsNeedingImprovement() {
        log.info("Identifying questions that need improvement");
        
        // TODO: Implement query to find questions with accuracy < 40%
        // SELECT q.question_id, 
        //        COUNT(CASE WHEN sa.is_correct = true THEN 1 END) * 1.0 / COUNT(*) as accuracy
        // FROM questions q
        // JOIN submission_answers sa ON q.question_id = sa.question_id
        // GROUP BY q.question_id
        // HAVING accuracy < 0.4
        // ORDER BY accuracy ASC
        
        return new ArrayList<>();
    }
}
