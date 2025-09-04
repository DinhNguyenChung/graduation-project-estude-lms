package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Assignment;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignment_ClassSubject_ClassSubjectId(Long classSubjectId);
    // Đếm số lần submission của một student cho 1 assignment
    int countByAssignmentAndStudent(Assignment assignment, Student student);

    // Nếu cần lấy list để check chi tiết:
    List<Submission> findByAssignmentAndStudentOrderBySubmittedAtDesc(Assignment assignment, Student student);
    Optional<Submission> findBySubmissionId(Long submissionId);


}