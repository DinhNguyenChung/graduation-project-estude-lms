package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByAssignmentAssignmentIdOrderByQuestionOrder(Long assignmentId);
}
