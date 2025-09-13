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
    @Query("SELECT cs.subject FROM ClassSubject cs WHERE cs.term.clazz.classId = :classId")
    List<Subject> findSubjectsByClassId(@Param("classId") Long classId);
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.schools sc WHERE sc.schoolId = :schoolId")
    List<Subject> findSubjectsBySchoolId(@Param("schoolId") Long schoolId);
    // Kiểm tra môn học có tồn tại với name và school_id
//    boolean existsByNameAndSchoolSchoolId(String name, Long schoolId);
    // Kiểm tra trùng lặp tên môn học trong một trường
    boolean existsByNameAndSchoolsSchoolId(String name, Long schoolId);


}
