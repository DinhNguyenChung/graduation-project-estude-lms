package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findBySessionSessionId(Long sessionId);
    Optional<AttendanceRecord> findBySessionSessionIdAndStudent_UserId(Long sessionId, Long studentId);

    List<AttendanceRecord> findByStudent_UserId(Long studentUserId);
}

