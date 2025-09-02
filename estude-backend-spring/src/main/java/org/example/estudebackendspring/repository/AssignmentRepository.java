package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    boolean existsByAssignmentIdAndSubmissionsNotEmpty(Long assignmentId);
//    List<Assignment> findAssignmentsByCl(Long studentId);
        @Query("SELECT a FROM Assignment a WHERE a.classSubject.clazz.classId IN :classIds AND (a.isPublished IS NULL OR a.isPublished = true)")
        List<Assignment> findPublishedByClassIds(@Param("classIds") List<Long> classIds);
}