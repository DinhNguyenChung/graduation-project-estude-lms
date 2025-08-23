package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
}
