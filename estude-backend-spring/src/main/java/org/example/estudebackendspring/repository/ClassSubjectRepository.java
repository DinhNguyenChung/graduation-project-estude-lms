package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {
    
    /**
     * Lấy tất cả ClassSubject với EAGER FETCH để tránh LazyInitializationException
     */
    @Query("SELECT cs FROM ClassSubject cs " +
           "LEFT JOIN FETCH cs.term t " +
           "LEFT JOIN FETCH t.clazz c " +
           "LEFT JOIN FETCH cs.subject s " +
           "LEFT JOIN FETCH cs.teacher teacher")
    List<ClassSubject> findAllWithDetails();
    
    // Lấy tất cả môn học mà teacher dạy
    List<ClassSubject> findByTeacher_UserId(Long teacherId);
    
    /**
     * Lấy tất cả ClassSubject của teacher với EAGER FETCH term và clazz
     * Dùng cho analytics để tránh LazyInitializationException
     */
    @Query("SELECT cs FROM ClassSubject cs " +
           "JOIN FETCH cs.term t " +
           "JOIN FETCH t.clazz c " +
           "JOIN FETCH cs.subject s " +
           "WHERE cs.teacher.userId = :teacherId")
    List<ClassSubject> findByTeacherUserIdWithTermAndClass(@Param("teacherId") Long teacherId);
    
    boolean existsByTerm_ClazzAndSubject(Clazz clazz, Subject subject);
    // Lấy classSubject theo classId
    List<ClassSubject> findByTerm_Clazz_ClassId(Long classId);
    
    /**
     * Lấy ClassSubject theo classId với EAGER FETCH để tránh lazy loading
     */
    @Query("SELECT DISTINCT cs FROM ClassSubject cs " +
           "LEFT JOIN FETCH cs.subject s " +
           "LEFT JOIN FETCH cs.teacher t " +
           "LEFT JOIN FETCH cs.term term " +
           "LEFT JOIN FETCH term.clazz c " +
           "WHERE c.classId = :classId")
    List<ClassSubject> findByClassIdWithDetails(@Param("classId") Long classId);
    
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
    
    /**
     * Lấy ClassSubject theo ID với EAGER FETCH để tránh lazy loading
     */
    @Query("SELECT cs FROM ClassSubject cs " +
           "LEFT JOIN FETCH cs.subject s " +
           "LEFT JOIN FETCH cs.teacher t " +
           "LEFT JOIN FETCH cs.term term " +
           "LEFT JOIN FETCH term.clazz c " +
           "WHERE cs.classSubjectId = :classSubjectId")
    Optional<ClassSubject> findByIdWithDetails(@Param("classSubjectId") Long classSubjectId);

    boolean existsByTermAndSubject(Term term, Subject subject);

    @Query("""
        SELECT e.student.userId
        FROM Enrollment e
        JOIN e.clazz c
        JOIN ClassSubject cs ON cs.term.clazz.classId = c.classId
        WHERE cs.classSubjectId = :csId
    """)
    List<Long> findStudentUserIdsByClassSubjectId(@Param("csId") Long classSubjectId);
}