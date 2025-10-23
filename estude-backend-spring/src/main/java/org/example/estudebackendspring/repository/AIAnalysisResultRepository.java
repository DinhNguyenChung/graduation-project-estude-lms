package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.AIAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AIAnalysisResultRepository extends JpaRepository<AIAnalysisResult, Long> {
    /**
     * Lấy kết quả mới nhất của student theo analysis type
     */
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
    
    /**
     * Lấy TẤT CẢ kết quả của student theo analysis type (ORDER BY mới nhất)
     */
    @Query(value = """
    SELECT r.result_id, r.predicted_average, r.predicted_performance,
           r.actual_performance, r.comment, r.suggested_actions,
           r.detailed_analysis, r.statistics, r.generated_at, r.request_id
    FROM ai_analysis_results r
    JOIN ai_analysis_requests req ON r.request_id = req.request_id
    WHERE req.student_id = :studentId 
      AND req.analysis_type = :analysisType
    ORDER BY r.generated_at DESC
    """, nativeQuery = true)
    List<AIAnalysisResult> findAllByStudentIdAndAnalysisType(
            @Param("studentId") Long studentId,
            @Param("analysisType") String analysisType
    );
    
    /**
     * Lấy kết quả mới nhất theo student + assignment (dùng cho các API cũ)
     */
    @Query(value = """
        SELECT r.* 
        FROM ai_analysis_results r
        JOIN ai_analysis_requests req ON r.request_id = req.request_id
        WHERE req.student_id = :studentId
          AND (req.data_payload::jsonb ->> 'assignment_id') = :assignmentId
        ORDER BY req.request_date DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<AIAnalysisResult> findLatestByStudentAndAssignment(
            @Param("studentId") Long studentId,
            @Param("assignmentId") String assignmentId
    );
    
    /**
     * Lấy TẤT CẢ kết quả theo student + assignment + analysis type
     * Xử lý assignment_id ở cả root level VÀ nested trong feedback_data
     */
//    @Query(value = """
//    SELECT r.result_id,
//           r.predicted_average,
//           r.predicted_performance,
//           r.actual_performance,
//           r.comment,
//           r.suggested_actions,
//           r.detailed_analysis,
//           r.statistics,
//           r.generated_at,
//           r.request_id
//    FROM ai_analysis_results r
//    JOIN ai_analysis_requests req
//        ON r.request_id = req.request_id
//    WHERE req.student_id = :studentId
//      AND req.analysis_type = :analysisType
//      AND (
//        (req.data_payload::jsonb ->> 'assignment_id') = CAST(:assignmentId AS TEXT)
//        OR
//        (req.data_payload::jsonb -> 'feedback_data' ->> 'assignment_id') = CAST(:assignmentId AS TEXT)
//      )
//    ORDER BY r.generated_at DESC
//    """, nativeQuery = true)
//    List<AIAnalysisResult> findAllByStudentAndAssignmentAndAnalysisType(
//            @Param("studentId") Long studentId,
//            @Param("assignmentId") String assignmentId,
//            @Param("analysisType") String analysisType
//    );

    @Query(value = """
    SELECT r.*
    FROM ai_analysis_results r
    JOIN ai_analysis_requests req 
        ON r.request_id = req.request_id
    WHERE req.student_id = :studentId 
      AND req.analysis_type = :analysisType
      AND (
        (req.data_payload::jsonb ->> 'assignment_id') = CAST(:assignmentId AS TEXT)
        OR 
        (req.data_payload::jsonb -> 'feedback_data' ->> 'assignment_id') = CAST(:assignmentId AS TEXT)
      )
    ORDER BY r.generated_at DESC
    """, nativeQuery = true)
    List<AIAnalysisResult> findAllByStudentAndAssignmentAndAnalysisType(
            @Param("studentId") Long studentId,
            @Param("assignmentId") String assignmentId,
            @Param("analysisType") String analysisType
    );



}
