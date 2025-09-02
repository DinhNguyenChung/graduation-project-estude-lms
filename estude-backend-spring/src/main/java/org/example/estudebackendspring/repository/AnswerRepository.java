package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {}