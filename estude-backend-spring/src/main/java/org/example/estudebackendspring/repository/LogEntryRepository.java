package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
}
