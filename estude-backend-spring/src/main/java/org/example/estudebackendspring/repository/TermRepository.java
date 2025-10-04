package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {
    @Query("SELECT t.clazz.classId FROM Term t WHERE t.termId = :termId")
    Optional<Long> findClassIdByTermId(@Param("termId") Long termId);
}
