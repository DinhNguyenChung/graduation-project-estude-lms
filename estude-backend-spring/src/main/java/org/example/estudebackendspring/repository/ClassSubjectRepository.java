package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {
    // Lấy tất cả môn học mà teacher dạy
    List<ClassSubject> findByTeacher_UserId(Long teacherId);
    boolean existsByClazzAndSubject(Clazz clazz, Subject subject);
    // Lấy classSubject theo classId
    List<ClassSubject> findByClazz_ClassId(Long classId);


}