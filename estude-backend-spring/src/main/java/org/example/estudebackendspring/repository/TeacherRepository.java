package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByTeacherCode(String teacherCode);
    Optional<Teacher> findByEmail(String email);
    Teacher findByUserId(Long teacherId);
    boolean existsByEmail(String email);
    boolean existsByNumberPhone(String phone);
    boolean existsByTeacherCode(String teacherCode);

}
