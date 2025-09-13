package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {
}
