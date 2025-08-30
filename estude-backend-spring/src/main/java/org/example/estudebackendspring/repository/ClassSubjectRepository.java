package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.ClassSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {
    // Lấy tất cả môn học mà teacher dạy
    List<ClassSubject> findByTeacher_UserId(Long teacherId);
}