package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.AIAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AIAnalysisResultRepository extends JpaRepository<AIAnalysisResult, Long> {
    @Query(value = """
    SELECT r.result_id, r.predicted_average, r.predicted_performance,
           r.actual_performance, r.comment, r.suggested_actions,
           r.detailed_analysis, r.statistics, r.generated_at, r.request_id
    FROM ai_analysis_results r
    JOIN ai_analysis_requests req ON r.request_id = req.request_id
    WHERE req.student_id = :studentId 
      AND req.analysis_type = :analysisType
    ORDER BY r.generated_at DESC
    LIMIT 1
    """, nativeQuery = true)
    AIAnalysisResult findLatestResultByStudentId(
            @Param("studentId") Long studentId,
            @Param("analysisType") String analysisType
    );


}
