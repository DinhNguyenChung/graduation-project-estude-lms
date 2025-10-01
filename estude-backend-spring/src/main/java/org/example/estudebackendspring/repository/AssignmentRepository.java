package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

//        @Query("SELECT COUNT(a) FROM Assignment a WHERE a.classSubject.term.termId = :termId AND a.classSubject IN " +
//                "(SELECT cs FROM ClassSubject cs WHERE cs.term.termId = :termId AND cs.classSubjectId IN " +
//                "(SELECT cs2.classSubjectId FROM ClassSubject cs2 JOIN cs2.assignments assign WHERE assign.teacher.userId = :studentId))")
//        long countByStudentAndTerm(@Param("studentId") Long studentId, @Param("termId") Long termId);
@Query("SELECT COUNT(DISTINCT a) " +
        "FROM Assignment a " +
        "JOIN a.classSubject cs " +
        "JOIN cs.term t " +
        "JOIN t.clazz c " +
        "JOIN c.enrollments e " +
        "WHERE e.student.userId = :studentId " +
        "AND t.termId = :termId " +
        "AND (a.isPublished = true OR a.isPublished IS NULL)")
long countByStudentAndTerm(@Param("studentId") Long studentId, @Param("termId") Long termId);


}