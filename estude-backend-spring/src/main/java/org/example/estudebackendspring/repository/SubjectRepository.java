package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    boolean existsByName(String name);

    boolean existsById(Long subjectId);
    Optional<Subject> findBySubjectId(Long subjectId);

}
