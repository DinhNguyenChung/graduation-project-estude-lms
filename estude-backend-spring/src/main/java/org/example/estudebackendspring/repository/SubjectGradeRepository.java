package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.SubjectGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubjectGradeRepository extends JpaRepository<SubjectGrade, Long> {

    @Query("SELECT DISTINCT sg FROM SubjectGrade sg " +
            "JOIN FETCH sg.classSubject cs " +
            "JOIN FETCH cs.subject subj " +
            "WHERE sg.student.userId = :studentId")
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
}