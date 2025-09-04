package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    boolean existsByName(String name);

    boolean existsById(Long subjectId);
    Optional<Subject> findBySubjectId(Long subjectId);
    // Tìm tất cả môn học theo classId thông qua bảng trung gian class_subjects
    @Query("SELECT cs.subject FROM ClassSubject cs WHERE cs.clazz.classId = :classId")
    List<Subject> findSubjectsByClassId(@Param("classId") Long classId);
    // Tìm tất cả môn học theo schoolId thông qua class_subject thông qua classes
    // School -> class -> Class Subject ->  Môn học
    @Query("SELECT DISTINCT s FROM Subject s " +
            "JOIN ClassSubject cs ON s.subjectId = cs.subject.subjectId " +
            "JOIN Clazz c ON cs.clazz.classId = c.classId " +
            "WHERE c.school.schoolId = :schoolId")
    List<Subject> findSubjectsBySchoolId(@Param("schoolId") Long schoolId);

}
