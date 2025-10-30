package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.AssessmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentSubmissionRepository extends JpaRepository<AssessmentSubmission, Long> {
    
    /**
     * Find all submissions by student
     */
    List<AssessmentSubmission> findByStudent_UserIdOrderBySubmittedAtDesc(Long studentId);
    
    /**
     * Find submissions by student and subject
     */
    List<AssessmentSubmission> findByStudent_UserIdAndSubject_SubjectIdOrderBySubmittedAtDesc(
        Long studentId, Long subjectId);
    
    /**
     * Find submission by assessment ID
     */
    Optional<AssessmentSubmission> findByAssessmentId(String assessmentId);
    
    /**
     * Check if student has already submitted this assessment
     */
    boolean existsByAssessmentIdAndStudent_UserId(String assessmentId, Long studentId);
    
    /**
     * Get latest submission for student in a subject
     */
    @Query("SELECT a FROM AssessmentSubmission a WHERE a.student.userId = :studentId " +
           "AND a.subject.subjectId = :subjectId ORDER BY a.submittedAt DESC")
    List<AssessmentSubmission> findLatestByStudentAndSubject(
        @Param("studentId") Long studentId, 
        @Param("subjectId") Long subjectId);
    
    /**
     * Count total submissions by student
     */
    long countByStudent_UserId(Long studentId);
}
