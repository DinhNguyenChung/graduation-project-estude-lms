package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Assignment;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignment_ClassSubject_ClassSubjectId(Long classSubjectId);
    // Đếm số lần submission của một student cho 1 assignment
    int countByAssignmentAndStudent(Assignment assignment, Student student);

    // Nếu cần lấy list để check chi tiết:
    List<Submission> findByAssignmentAndStudentOrderBySubmittedAtDesc(Assignment assignment, Student student);
    Optional<Submission> findBySubmissionId(Long submissionId);
    // Lấy tất cả submission theo student
    List<Submission> findByStudent_userId(Long studentId);

    // Lấy tất cả submission theo student và assignment
    List<Submission> findByStudent_userIdAndAssignment_AssignmentId(Long studentId, Long assignmentId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.student.userId = :studentId AND s.assignment.classSubject.term.termId = :termId")
    long countByStudentAndTerm(@Param("studentId") Long studentId, @Param("termId") Long termId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.student.userId = :studentId AND s.assignment.classSubject.term.termId = :termId AND s.isLate = true")
    long countLateByStudentAndTerm(@Param("studentId") Long studentId, @Param("termId") Long termId);
}
