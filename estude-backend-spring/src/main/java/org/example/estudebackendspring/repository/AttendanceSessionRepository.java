package org.example.estudebackendspring.repository;


import org.example.estudebackendspring.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    List<AttendanceSession> findByClassSubjectClassSubjectId(Long classSubjectId);
    List<AttendanceSession> findByTeacher_UserId(Long teacherId);
    
    /**
     * Fetch AttendanceSession with ClassSubject details to avoid lazy loading
     */
    @Query("SELECT DISTINCT s FROM AttendanceSession s " +
           "LEFT JOIN FETCH s.classSubject cs " +
           "LEFT JOIN FETCH cs.term t " +
           "LEFT JOIN FETCH t.clazz c " +
           "LEFT JOIN FETCH cs.subject " +
           "LEFT JOIN FETCH s.teacher " +
           "WHERE cs.classSubjectId = :classSubjectId " +
           "ORDER BY s.startTime DESC")
    List<AttendanceSession> findByClassSubjectIdWithDetails(@Param("classSubjectId") Long classSubjectId);
}
