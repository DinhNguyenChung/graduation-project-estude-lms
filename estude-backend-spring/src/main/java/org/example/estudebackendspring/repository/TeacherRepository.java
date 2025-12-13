package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByTeacherCode(String teacherCode);
    Optional<Teacher> findByEmail(String email);
    Teacher findByUserId(Long teacherId);
    boolean existsByEmail(String email);
    boolean existsByNumberPhone(String phone);
    boolean existsByTeacherCode(String teacherCode);
    
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.school ORDER BY t.fullName")
    List<Teacher> findAllWithSchool();

}
