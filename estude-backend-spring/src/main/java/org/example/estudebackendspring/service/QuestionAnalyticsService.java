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
     */
    @Cacheable(value = "questionBankStats", unless = "#result == null")
    public QuestionBankStatisticsDTO getQuestionBankOverview() {
        log.info("Fetching question bank overview statistics");
        
        List<Question> allQuestions = questionRepository.findByIsQuestionBankTrueOrderByQuestionIdDesc();
        
        int totalQuestions = allQuestions.size();
        
        // Count active questions (those with options and correct answers)
        long activeQuestions = allQuestions.stream()
                .filter(q -> q.getOptions() != null && !q.getOptions().isEmpty())
                .count();
        
        int inactiveQuestions = totalQuestions - (int) activeQuestions;
        
        // Group by difficulty
        Map<String, Integer> byDifficulty = allQuestions.stream()
                .collect(Collectors.groupingBy(
                        q -> q.getDifficultyLevel() != null ? q.getDifficultyLevel().name() : "UNKNOWN",
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        // Group by subject (through topic)
        Map<String, Integer> bySubject = allQuestions.stream()
                .filter(q -> q.getTopic() != null && q.getTopic().getSubject() != null)
                .collect(Collectors.groupingBy(
                        q -> q.getTopic().getSubject().getName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        // Group by topic
        Map<String, Integer> byTopic = allQuestions.stream()
                .filter(q -> q.getTopic() != null)
                .collect(Collectors.groupingBy(
                        q -> q.getTopic().getName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        log.info("Question bank overview: total={}, active={}, inactive={}", 
                totalQuestions, activeQuestions, inactiveQuestions);
        
        return QuestionBankStatisticsDTO.builder()
                .totalQuestions(totalQuestions)
                .activeQuestions((int) activeQuestions)
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
