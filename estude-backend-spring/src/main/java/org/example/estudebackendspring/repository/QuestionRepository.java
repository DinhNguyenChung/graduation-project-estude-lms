package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.dto.QuestionBankSummaryDTO;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.enums.DifficultyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByAssignmentAssignmentIdOrderByQuestionOrder(Long assignmentId);
    
    // ========== QUESTION BANK QUERIES - OPTIMIZED ==========
    
    /**
     * Lấy tất cả câu hỏi trong question bank với pagination và JOIN FETCH
     * Eager load topic và options để tránh N+1 query problem
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.topic t " +
           "LEFT JOIN FETCH t.subject " +
           "LEFT JOIN FETCH q.options " +
           "WHERE q.isQuestionBank = true " +
           "ORDER BY q.questionId DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Page<Question> findAllQuestionBankWithDetails(Pageable pageable);
    
    /**
     * Lấy tất cả câu hỏi trong question bank - Summary DTO (tối ưu hơn)
     * Projection query - chỉ select các trường cần thiết
     */
    @Query("SELECT NEW org.example.estudebackendspring.dto.QuestionBankSummaryDTO(" +
           "q.questionId, q.questionText, q.points, q.questionType, q.difficultyLevel, " +
           "t.name, s.name, COUNT(o.optionId)) " +
           "FROM Question q " +
           "LEFT JOIN q.topic t " +
           "LEFT JOIN t.subject s " +
           "LEFT JOIN q.options o " +
           "WHERE q.isQuestionBank = true " +
           "GROUP BY q.questionId, q.questionText, q.points, q.questionType, q.difficultyLevel, t.name, s.name " +
           "ORDER BY q.questionId DESC")
    Page<QuestionBankSummaryDTO> findAllQuestionBankSummary(Pageable pageable);
    
    /**
     * Lấy câu hỏi trong question bank theo topic với pagination
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.topic t " +
           "LEFT JOIN FETCH t.subject " +
           "LEFT JOIN FETCH q.options " +
           "WHERE q.topic.topicId = :topicId AND q.isQuestionBank = true " +
           "ORDER BY q.questionId DESC")
    Page<Question> findQuestionBankByTopicWithDetails(@Param("topicId") Long topicId, Pageable pageable);
    
    /**
     * Lấy câu hỏi trong question bank theo topic - Summary DTO
     */
    @Query("SELECT NEW org.example.estudebackendspring.dto.QuestionBankSummaryDTO(" +
           "q.questionId, q.questionText, q.points, q.questionType, q.difficultyLevel, " +
           "t.name, s.name, COUNT(o.optionId)) " +
           "FROM Question q " +
           "LEFT JOIN q.topic t " +
           "LEFT JOIN t.subject s " +
           "LEFT JOIN q.options o " +
           "WHERE q.topic.topicId = :topicId AND q.isQuestionBank = true " +
           "GROUP BY q.questionId, q.questionText, q.points, q.questionType, q.difficultyLevel, t.name, s.name " +
           "ORDER BY q.questionId DESC")
    Page<QuestionBankSummaryDTO> findQuestionBankByTopicSummary(@Param("topicId") Long topicId, Pageable pageable);
    
    /**
     * Lấy câu hỏi trong question bank theo topic và độ khó với pagination
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.topic t " +
           "LEFT JOIN FETCH t.subject " +
           "LEFT JOIN FETCH q.options " +
           "WHERE q.topic.topicId = :topicId " +
           "AND q.difficultyLevel = :difficultyLevel " +
           "AND q.isQuestionBank = true " +
           "ORDER BY q.questionId DESC")
    Page<Question> findQuestionBankByTopicAndDifficultyWithDetails(
        @Param("topicId") Long topicId, 
        @Param("difficultyLevel") DifficultyLevel difficultyLevel,
        Pageable pageable);
    
    /**
     * Lấy một question với tất cả relationships (để tránh lazy loading)
     */
    @Query("SELECT q FROM Question q " +
           "LEFT JOIN FETCH q.topic t " +
           "LEFT JOIN FETCH t.subject " +
           "LEFT JOIN FETCH q.options " +
           "WHERE q.questionId = :questionId")
    Optional<Question> findByIdWithDetails(@Param("questionId") Long questionId);
    
    // ========== LEGACY METHODS (backward compatibility) ==========
    
    /**
     * @deprecated Use findAllQuestionBankSummary() with Pageable instead
     */
    @Deprecated
    List<Question> findByIsQuestionBankTrueOrderByQuestionIdDesc();
    
    /**
     * @deprecated Use findQuestionBankByTopicSummary() with Pageable instead
     */
    @Deprecated
    List<Question> findByTopic_TopicIdAndIsQuestionBankTrueOrderByQuestionIdDesc(Long topicId);
    
    /**
     * @deprecated Use findQuestionBankByTopicAndDifficultyWithDetails() with Pageable instead
     */
    @Deprecated
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
    
    // ========== FILTER BY SUBJECT AND GRADE ==========
    
    /**
     * Lấy questions theo subject với pagination - Summary DTO
     */
    @Query("SELECT NEW org.example.estudebackendspring.dto.QuestionBankSummaryDTO(" +
           "q.questionId, q.questionText, q.points, q.questionType, q.difficultyLevel, " +
           "t.name, s.name, COUNT(o.optionId)) " +
           "FROM Question q " +
           "LEFT JOIN q.topic t " +
           "LEFT JOIN t.subject s " +
           "LEFT JOIN q.options o " +
           "WHERE q.isQuestionBank = true AND t.subject.subjectId = :subjectId " +
           "GROUP BY q.questionId, q.questionText, q.points, q.questionType, q.difficultyLevel, t.name, s.name " +
           "ORDER BY q.questionId DESC")
    Page<QuestionBankSummaryDTO> findQuestionBankBySubjectSummary(@Param("subjectId") Long subjectId, Pageable pageable);
    
    /**
     * Lấy questions theo subject và grade level với pagination - Summary DTO
     * Filter by subject và grade level (enum: GRADE_10, GRADE_11, etc.)
     */
    @Query("SELECT NEW org.example.estudebackendspring.dto.QuestionBankSummaryDTO(" +
           "q.questionId, q.questionText, q.points, q.questionType, q.difficultyLevel, " +
           "t.name, s.name, COUNT(o.optionId)) " +
           "FROM Question q " +
           "LEFT JOIN q.topic t " +
           "LEFT JOIN t.subject s " +
           "LEFT JOIN q.options o " +
           "WHERE q.isQuestionBank = true " +
           "AND t.subject.subjectId = :subjectId " +
           "AND t.gradeLevel = :gradeLevel " +
           "GROUP BY q.questionId, q.questionText, q.points, q.questionType, q.difficultyLevel, t.name, s.name " +
           "ORDER BY q.questionId DESC")
    Page<QuestionBankSummaryDTO> findQuestionBankBySubjectAndGradeLevelSummary(
        @Param("subjectId") Long subjectId, 
        @Param("gradeLevel") org.example.estudebackendspring.enums.GradeLevel gradeLevel, 
        Pageable pageable);
    
    /**
     * Lấy questions theo grade level với pagination - Summary DTO
     * Filter by grade level (enum: GRADE_10, GRADE_11, etc.)
     */
    @Query("SELECT NEW org.example.estudebackendspring.dto.QuestionBankSummaryDTO(" +
           "q.questionId, q.questionText, q.points, q.questionType, q.difficultyLevel, " +
           "t.name, s.name, COUNT(o.optionId)) " +
           "FROM Question q " +
           "LEFT JOIN q.topic t " +
           "LEFT JOIN t.subject s " +
           "LEFT JOIN q.options o " +
           "WHERE q.isQuestionBank = true AND t.gradeLevel = :gradeLevel " +
           "GROUP BY q.questionId, q.questionText, q.points, q.questionType, q.difficultyLevel, t.name, s.name " +
           "ORDER BY q.questionId DESC")
    Page<QuestionBankSummaryDTO> findQuestionBankByGradeLevelSummary(
        @Param("gradeLevel") org.example.estudebackendspring.enums.GradeLevel gradeLevel, 
        Pageable pageable);
    
    /**
     * Lấy questions theo subject với optional topicId filter - Summary DTO
     */
    @Query("SELECT NEW org.example.estudebackendspring.dto.QuestionBankSummaryDTO(" +
           "q.questionId, q.questionText, q.points, q.questionType, q.difficultyLevel, " +
           "t.name, s.name, COUNT(o.optionId)) " +
           "FROM Question q " +
           "LEFT JOIN q.topic t " +
           "LEFT JOIN t.subject s " +
           "LEFT JOIN q.options o " +
           "WHERE q.isQuestionBank = true AND t.subject.subjectId = :subjectId " +
           "AND (:topicId IS NULL OR t.topicId = :topicId) " +
           "GROUP BY q.questionId, q.questionText, q.points, q.questionType, q.difficultyLevel, t.name, s.name " +
           "ORDER BY q.questionId DESC")
    Page<QuestionBankSummaryDTO> findQuestionBankBySubjectAndTopicSummary(
        @Param("subjectId") Long subjectId,
        @Param("topicId") Long topicId,
        Pageable pageable);
}
