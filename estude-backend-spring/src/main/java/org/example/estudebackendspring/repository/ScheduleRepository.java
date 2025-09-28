package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("SELECT s FROM Schedule s " +
            "JOIN FETCH s.classSubject cs " +
            "JOIN FETCH cs.teacher t " +
            "JOIN FETCH cs.subject " +
            "JOIN FETCH s.term term " +
            "WHERE cs.teacher.userId = :teacherId " +
            "AND term.beginDate <= CURRENT_DATE " +
            "AND term.endDate >= CURRENT_DATE")
    List<Schedule> findSchedulesForTeacher(@Param("teacherId") Long teacherId);

    @Query("SELECT DISTINCT s FROM Schedule s " +
            "JOIN FETCH s.classSubject cs " +
            "JOIN FETCH cs.term t " +
            "JOIN FETCH t.clazz c " +
            "JOIN FETCH c.enrollments e " +
            "JOIN FETCH cs.subject " +
            "JOIN FETCH cs.teacher " +
            "WHERE e.student.userId = :studentId " +
            "AND s.term = cs.term " +
            "AND t.beginDate <= CURRENT_DATE " +
            "AND t.endDate >= CURRENT_DATE")
    List<Schedule> findSchedulesForStudent(@Param("studentId") Long studentId);
    
    // Alternative method that might be more efficient through class subject
    List<Schedule> findByClassSubject_ClassSubjectId(Long classSubjectId);
}
