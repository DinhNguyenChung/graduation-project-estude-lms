package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubmissionReportRepository extends JpaRepository<Submission, Long> {

    @Query(value = """
        SELECT CAST(jsonb_build_object(
            'test_id', a.assignment_id,
            'student_name', u.full_name,
            'subject', sub.name,
            'questions', jsonb_agg(
                jsonb_build_object(
                    'question', q.question_text,
                    'options', (
                        SELECT jsonb_agg(o.option_text ORDER BY o.option_order)
                        FROM question_options o
                        WHERE o.question_id = q.question_id
                    ),
                    'correct_answer', (
                        SELECT o.option_order
                        FROM question_options o
                        WHERE o.question_id = q.question_id AND o.is_correct = TRUE
                        LIMIT 1
                    ),
                    'student_answer', (
                        SELECT o.option_order
                        FROM answers ans
                        JOIN question_options o ON ans.option_id = o.option_id
                        WHERE ans.question_id = q.question_id
                          AND ans.submission_id = s.submission_id
                        LIMIT 1
                    )
                )
            )
        ) AS TEXT) AS result_json
        FROM assignments a
        JOIN class_subjects cs ON cs.class_subject_id = a.class_subject_id
        JOIN subjects sub ON cs.subject_id = sub.subject_id
        JOIN submissions s ON s.assignment_id = a.assignment_id
        JOIN students st ON st.user_id = s.student_id
        JOIN users u ON u.user_id = st.user_id
        JOIN questions q ON q.assignment_id = a.assignment_id
        WHERE a.assignment_id = :assignmentId
          AND s.student_id = :studentId
          AND s.submission_id = (
              SELECT MAX(s2.submission_id)
              FROM submissions s2
              WHERE s2.assignment_id = :assignmentId
                AND s2.student_id = :studentId
          )
        GROUP BY a.assignment_id, u.full_name, sub.name
        """, nativeQuery = true)
    Optional<String> getStudentSubmissionJson(@Param("assignmentId") Long assignmentId,
                                              @Param("studentId") Long studentId);
}


