package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    
    /**
     * Lấy tất cả options cho danh sách question IDs (batch query để tránh N+1)
     */
    @Query("SELECT o FROM QuestionOption o WHERE o.question.questionId IN :questionIds ORDER BY o.optionOrder")
    List<QuestionOption> findByQuestionIdIn(@Param("questionIds") List<Long> questionIds);
}
