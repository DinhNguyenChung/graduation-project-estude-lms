package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, Long> {
}