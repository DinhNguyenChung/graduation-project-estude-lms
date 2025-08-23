package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // Method to find schedules by the student's class
    List<Schedule> findByClazz_Enrollments_Student_StudentCode(String studentCode);
    
    // Alternative method that might be more efficient through class subject
    List<Schedule> findByClassSubject_ClassSubjectId(Long classSubjectId);
}
