package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
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

    @Query("SELECT s FROM Schedule s " +
            "JOIN s.term t " +
            "JOIN s.classSubject cs " +
            "JOIN cs.subject subj " +
            "JOIN cs.teacher teacher " +
            "JOIN t.clazz c " +
            "WHERE c.classId = :classId " +
            "AND t.beginDate <= CURRENT_DATE " +
            "AND t.endDate >= CURRENT_DATE")
    List<Schedule> findSchedulesByClassIdAndCurrentTerm(@Param("classId") Long classId);
    @Query("SELECT s FROM Schedule s " +
            "JOIN s.term t " +
            "JOIN s.classSubject cs " +
            "JOIN cs.subject subj " +
            "JOIN cs.teacher teacher " +
            "JOIN t.clazz c " +
            "WHERE c.classId = :classId ")
    List<Schedule> findSchedulesByClassId(@Param("classId") Long classId);
    @Query("SELECT s FROM Schedule s " +
            "WHERE s.classSubject.classSubjectId = :classSubjectId " +
            "AND s.date = :date " +
            "AND ((:startPeriod BETWEEN s.startPeriod AND s.endPeriod) " +
            "   OR (:endPeriod BETWEEN s.startPeriod AND s.endPeriod) " +
            "   OR (s.startPeriod BETWEEN :startPeriod AND :endPeriod) " +
            "   OR (s.endPeriod BETWEEN :startPeriod AND :endPeriod))")
    List<Schedule> findConflictingSchedules(@Param("classSubjectId") Long classSubjectId,
                                            @Param("date") Date date,
                                            @Param("startPeriod") Integer startPeriod,
                                            @Param("endPeriod") Integer endPeriod);
    @Query("SELECT s FROM Schedule s " +
            "JOIN s.classSubject cs " +
            "JOIN cs.term t " +
            "JOIN t.clazz c " +
            "WHERE c.classId = :classId " +
            "AND s.date = :date " +
            "AND ((:startPeriod BETWEEN s.startPeriod AND s.endPeriod) " +
            "   OR (:endPeriod BETWEEN s.startPeriod AND s.endPeriod) " +
            "   OR (s.startPeriod BETWEEN :startPeriod AND :endPeriod) " +
            "   OR (s.endPeriod BETWEEN :startPeriod AND :endPeriod))")
    List<Schedule> findConflictingSchedulesForClass(@Param("classId") Long classId,
                                                    @Param("date") Date date,
                                                    @Param("startPeriod") Integer startPeriod,
                                                    @Param("endPeriod") Integer endPeriod);

    Schedule findScheduleByScheduleId(@Param("id") Long id);

}
