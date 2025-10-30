package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.AssessmentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentAnswerRepository extends JpaRepository<AssessmentAnswer, Long> {
    
    /**
     * Find all answers for a submission
     */
    List<AssessmentAnswer> findByAssessmentSubmission_SubmissionId(Long submissionId);
    
    /**
     * Get topic-wise statistics for a student
     * Returns: topic_name, total_questions, correct_answers, accuracy
     * Only includes submissions that have been improvement evaluated (improvementEvaluated = true)
     */
    @Query("""
        SELECT 
            t.name as topicName,
            COUNT(aa.answerId) as totalQuestions,
            SUM(CASE WHEN aa.isCorrect = true THEN 1 ELSE 0 END) as correctAnswers,
            CAST(SUM(CASE WHEN aa.isCorrect = true THEN 1.0 ELSE 0.0 END) / COUNT(aa.answerId) as double) as accuracy,
            COUNT(DISTINCT aa.assessmentSubmission.submissionId) as assessmentCount
        FROM AssessmentAnswer aa
        JOIN aa.topic t
        JOIN aa.assessmentSubmission asub
        WHERE asub.student.userId = :studentId
        AND asub.improvementEvaluated = true
        GROUP BY t.name
        ORDER BY t.name
    """)
    List<Object[]> findTopicStatisticsByStudentId(@Param("studentId") Long studentId);
    
    /**
     * Get topic-wise statistics for a student filtered by subject
     * Only includes submissions that have been improvement evaluated (improvementEvaluated = true)
     */
    @Query("""
        SELECT 
            t.name as topicName,
            COUNT(aa.answerId) as totalQuestions,
            SUM(CASE WHEN aa.isCorrect = true THEN 1 ELSE 0 END) as correctAnswers,
            CAST(SUM(CASE WHEN aa.isCorrect = true THEN 1.0 ELSE 0.0 END) / COUNT(aa.answerId) as double) as accuracy,
            COUNT(DISTINCT aa.assessmentSubmission.submissionId) as assessmentCount
        FROM AssessmentAnswer aa
        JOIN aa.topic t
        JOIN aa.assessmentSubmission asub
        WHERE asub.student.userId = :studentId
        AND asub.subject.subjectId = :subjectId
        AND asub.improvementEvaluated = true
        GROUP BY t.name
        ORDER BY t.name
    """)
    List<Object[]> findTopicStatisticsByStudentIdAndSubjectId(
        @Param("studentId") Long studentId,
        @Param("subjectId") Long subjectId
    );
}
