package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {
    // Lấy tất cả môn học mà teacher dạy
    List<ClassSubject> findByTeacher_UserId(Long teacherId);
    boolean existsByTerm_ClazzAndSubject(Clazz clazz, Subject subject);
    // Lấy classSubject theo classId
    List<ClassSubject> findByTerm_Clazz_ClassId(Long classId);
    @Query("SELECT cs.subject FROM ClassSubject cs WHERE cs.teacher.userId = :teacherId")
    List<Subject> findSubjectsByTeacherId(@Param("teacherId") Long teacherId);
    @Query("SELECT e.student FROM ClassSubject cs " +
            "JOIN cs.term.clazz c " +
            "JOIN c.enrollments e " +
            "WHERE cs.teacher.userId = :teacherId AND cs.subject.subjectId = :subjectId")
    List<Student> findStudentsByTeacherAndSubject(@Param("teacherId") Long teacherId,
                                                  @Param("subjectId") Long subjectId);
    // lấy môn lớp học theo mã và teacher
    Optional<ClassSubject> findByClassSubjectIdAndTeacher_UserId(Long classId, Long teacherId);
    Optional<ClassSubject> findByClassSubjectId(Long id);


    boolean existsByTermAndSubject(Term term, Subject subject);
}