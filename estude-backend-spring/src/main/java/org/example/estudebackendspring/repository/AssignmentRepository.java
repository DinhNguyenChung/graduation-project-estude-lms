package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    boolean existsByAssignmentIdAndSubmissionsNotEmpty(Long assignmentId);
//    List<Assignment> findAssignmentsByCl(Long studentId);
}