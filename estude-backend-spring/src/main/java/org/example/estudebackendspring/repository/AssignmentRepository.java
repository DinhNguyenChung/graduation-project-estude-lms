package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    boolean existsByAssignmentIdAndSubmissionsNotEmpty(Long assignmentId);
//    List<Assignment> findAssignmentsByCl(Long studentId);
        @Query("SELECT a FROM Assignment a WHERE a.classSubject.term.clazz.classId IN :classIds AND (a.isPublished IS NULL OR a.isPublished = true)")
        List<Assignment> findPublishedByClassIds(@Param("classIds") List<Long> classIds);
        // Lấy assignment theo classId
        @Query("SELECT a FROM Assignment a WHERE a.classSubject.term.clazz.classId = :classId")
        List<Assignment> findByClassId(@Param("classId") Long classId);
        List<Assignment> findAssignmentsByClassSubject_ClassSubjectId(Long classId);
}