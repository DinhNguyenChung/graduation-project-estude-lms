package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.enums.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByAssignmentAssignmentIdOrderByQuestionOrder(Long assignmentId);
    
    // ========== QUESTION BANK QUERIES ==========
    
    /**
     * Lấy tất cả câu hỏi trong question bank
     */
    List<Question> findByIsQuestionBankTrueOrderByQuestionIdDesc();
    
    /**
     * Lấy câu hỏi trong question bank theo topic
     */
    List<Question> findByTopic_TopicIdAndIsQuestionBankTrueOrderByQuestionIdDesc(Long topicId);
    
    /**
     * Lấy câu hỏi trong question bank theo topic và độ khó
     */
    List<Question> findByTopic_TopicIdAndDifficultyLevelAndIsQuestionBankTrue(
        Long topicId, DifficultyLevel difficultyLevel);
    
    /**
     * Đếm số câu hỏi trong question bank của một topic
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.topic.topicId = :topicId AND q.isQuestionBank = true")
    Long countQuestionsByTopicId(@Param("topicId") Long topicId);
    
    /**
     * Đếm số câu hỏi trong question bank theo topic và độ khó
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.topic.topicId = :topicId " +
           "AND q.difficultyLevel = :difficultyLevel AND q.isQuestionBank = true")
    Long countQuestionsByTopicIdAndDifficulty(
        @Param("topicId") Long topicId, 
        @Param("difficultyLevel") DifficultyLevel difficultyLevel);
}
