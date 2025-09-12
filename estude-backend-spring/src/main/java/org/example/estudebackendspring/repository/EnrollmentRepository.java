package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.AttendanceRecord;
import org.example.estudebackendspring.entity.Enrollment;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByClazzAndStudent(Clazz clazz, Student student);

    List<Enrollment> findByStudent_UserId(Long studentUserId);
    @Query("SELECT e.clazz.classId FROM Enrollment e WHERE e.student.userId = :studentId")
    List<Long> findClassIdsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e WHERE e.student.userId = :studentId AND e.clazz.classId = :classId")
    boolean existsByStudentIdAndClassId(@Param("studentId") Long studentId, @Param("classId") Long classId);
    int countByClazz(Clazz clazz);
    @Query("select e.student from Enrollment e where e.clazz.classId = :classId")
    List<Student> findStudentsByClazzId(@Param("classId") Long classId);

    boolean existsByClazz_ClassIdAndStudent_UserId(Long classId, Long studentId);
    List<Enrollment> findByClazzClassId(Long classId);
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Enrollment e " +
            "JOIN e.clazz c " +
            "WHERE e.student = :student " +
            "AND (c.beginDate <= :endDate AND c.endDate >= :beginDate)")
    boolean existsEnrollmentConflict(@Param("student") Student student,
                                     @Param("beginDate") Date beginDate,
                                     @Param("endDate") Date endDate);




}