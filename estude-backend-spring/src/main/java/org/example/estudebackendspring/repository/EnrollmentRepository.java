package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Enrollment;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByClazzAndStudent(Clazz clazz, Student student);
}