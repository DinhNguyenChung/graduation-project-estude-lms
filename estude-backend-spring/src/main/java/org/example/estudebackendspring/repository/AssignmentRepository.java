package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    boolean existsByAssignmentIdAndSubmissionsNotEmpty(Long assignmentId);
}