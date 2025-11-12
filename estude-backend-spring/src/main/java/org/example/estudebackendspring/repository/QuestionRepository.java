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
    
    // ========== ANALYTICS NATIVE QUERIES (Tối ưu N+1 Query Problem) ==========
    
    /**
     * Đếm tổng số câu hỏi question bank
     */
    Long countByIsQuestionBankTrue();
    
    /**
     * Đếm số câu hỏi theo difficulty level
     * Returns: [difficulty_level, count]
     */
    @Query(value = """
        SELECT difficulty_level, COUNT(*) as count
        FROM questions
        WHERE is_question_bank = true
        GROUP BY difficulty_level
        """, nativeQuery = true)
    List<Object[]> countByDifficultyLevel();
    
    /**
     * Đếm số câu hỏi theo subject
     * Returns: [subject_name, count]
     */
    @Query(value = """
        SELECT s.name, COUNT(q.question_id) as count
        FROM questions q
        JOIN topics t ON q.topic_id = t.topic_id
        JOIN subjects s ON t.subject_id = s.subject_id
        WHERE q.is_question_bank = true
        GROUP BY s.name
        """, nativeQuery = true)
    List<Object[]> countBySubject();
    
    /**
     * Đếm số câu hỏi theo topic
     * Returns: [topic_name, count]
     */
    @Query(value = """
        SELECT t.name, COUNT(q.question_id) as count
        FROM questions q
        JOIN topics t ON q.topic_id = t.topic_id
        WHERE q.is_question_bank = true
        GROUP BY t.name
        """, nativeQuery = true)
    List<Object[]> countByTopic();
}
