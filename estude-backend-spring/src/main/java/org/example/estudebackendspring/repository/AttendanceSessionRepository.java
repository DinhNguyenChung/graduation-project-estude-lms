package org.example.estudebackendspring.repository;


import org.example.estudebackendspring.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    List<AttendanceSession> findByClassSubjectClassSubjectId(Long classSubjectId);
    List<AttendanceSession> findByTeacher_UserId(Long teacherId);
}
