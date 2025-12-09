package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
    
    /**
     * Fetch all log entries with user eagerly to avoid lazy loading issues
     */
    @Query("SELECT l FROM LogEntry l LEFT JOIN FETCH l.user ORDER BY l.timestamp DESC")
    List<LogEntry> findAllWithUser();
}
