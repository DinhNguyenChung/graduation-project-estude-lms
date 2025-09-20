package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.AttendanceRecord;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findBySessionSessionId(Long sessionId);
    Optional<AttendanceRecord> findBySessionSessionIdAndStudent_UserId(Long sessionId, Long studentId);

    List<AttendanceRecord> findByStudent_UserId(Long studentUserId);

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.student.userId = :studentId AND ar.session.classSubject.term.termId = :termId")
    long countByStudentAndTerm(@Param("studentId") Long studentId, @Param("termId") Long termId);

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.student.userId = :studentId AND ar.status = 'ABSENT' AND ar.session.classSubject.term.termId = :termId")
    long countAbsentByStudentAndTerm(@Param("studentId") Long studentId, @Param("termId") Long termId);

}

