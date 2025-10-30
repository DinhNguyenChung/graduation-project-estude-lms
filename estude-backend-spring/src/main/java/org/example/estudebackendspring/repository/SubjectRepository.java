package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Subject;
import org.example.estudebackendspring.enums.GradeLevel;
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
    
    // Note: These queries are commented out as Subject-School relationship is now optional
    // Uncomment if you want to keep the school relationship
    /*
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.schools sc WHERE sc.schoolId = :schoolId")
    List<Subject> findSubjectsBySchoolId(@Param("schoolId") Long schoolId);
    
    // Kiểm tra môn học có tồn tại với name và school_id
    // boolean existsByNameAndSchoolSchoolId(String name, Long schoolId);
    
    // Kiểm tra trùng lặp tên môn học trong một trường
    boolean existsByNameAndSchoolsSchoolId(String name, Long schoolId);
    */
    
    // === DEPRECATED: Grade Level & Volume filtering has been moved to Topic level ===
    // Subject entity no longer has gradeLevel or volume fields.
    // Use TopicRepository methods instead for filtering by grade and volume.
    
    /*
    // These methods are no longer valid since Subject doesn't have gradeLevel/volume
    List<Subject> findByGradeLevel(GradeLevel gradeLevel);
    List<Subject> findByGradeLevelAndVolume(GradeLevel gradeLevel, Integer volume);
    boolean existsByNameAndGradeLevel(String name, GradeLevel gradeLevel);
    boolean existsByNameAndGradeLevelAndVolume(String name, GradeLevel gradeLevel, Integer volume);
    
    @Query("SELECT s FROM Subject s WHERE s.gradeLevel = :gradeLevel ORDER BY s.name, s.volume")
    List<Subject> findAllByGradeLevelOrderByNameAndVolume(@Param("gradeLevel") GradeLevel gradeLevel);
    */


}
