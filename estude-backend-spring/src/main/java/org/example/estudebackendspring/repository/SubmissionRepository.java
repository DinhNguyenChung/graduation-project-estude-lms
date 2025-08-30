package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignment_ClassSubject_ClassSubjectId(Long classSubjectId);

}