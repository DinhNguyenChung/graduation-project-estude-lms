package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.SubjectGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubjectGradeRepository extends JpaRepository<SubjectGrade, Long> {

    @Query(value = """

            SELECT DISTINCT sg.*\s
    FROM subject_grades sg
    JOIN class_subjects cs ON sg.class_subject_id = cs.class_subject_id
    JOIN subjects subj ON cs.subject_id = subj.subject_id
    JOIN terms t ON cs.term_id = t.term_id
    JOIN classes c ON t.class_id = c.class_id
    WHERE sg.student_id = :studentId
      AND CURRENT_DATE BETWEEN t.begin_date AND t.end_date;
    """, nativeQuery = true)
    List<SubjectGrade> findByStudentIdWithSubject(@Param("studentId") Long studentId);


    //    List<SubjectGrade> findByClassSubject_ClassSubjectId(Long classSubjectId);
@Query("SELECT sg FROM SubjectGrade sg " +
        "JOIN FETCH sg.student s " +
        "JOIN FETCH sg.classSubject cs " +
        "JOIN FETCH cs.subject subj " +
        "WHERE cs.classSubjectId = :classSubjectId")
List<SubjectGrade> findByClassSubjectIdWithStudent(@Param("classSubjectId") Long classSubjectId);
    Optional<SubjectGrade> findByStudent_UserIdAndClassSubject_ClassSubjectId(Long studentUserId, Long classSubjectId);
    List<SubjectGrade> findByClassSubject_ClassSubjectId(Long classSubjectId);
    /**
     * Lấy tất cả SubjectGrade của 1 học sinh, fetch các relation cần thiết:
     * classSubject -> subject, term -> clazz, teacher
     */
    @Query("SELECT sg FROM SubjectGrade sg " +
            "JOIN FETCH sg.classSubject cs " +
            "JOIN FETCH cs.subject subj " +
            "JOIN FETCH cs.term t " +
            "LEFT JOIN FETCH cs.teacher teacher " +
            "LEFT JOIN FETCH t.clazz cl " +
            "WHERE sg.student.userId = :studentId " +
            "ORDER BY t.beginDate ASC, subj.name ASC")
    List<SubjectGrade> findAllByStudentIdFetchAll(@Param("studentId") Long studentId);
}