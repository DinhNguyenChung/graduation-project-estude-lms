package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentCode(String studentCode);
    // Lấy điểm theo studentId
    @Query(value = """
        SELECT 
            s.user_id AS studentId,
            s.student_code AS studentCode,
            u.full_name AS fullName,
            subj.name AS subjectName,
            sg.midterm_score AS midtermScore,
            sg.final_score AS finalScore,
            sg.actual_average AS actualAverage,
            sg.predicted_mid_term AS predictedMidTerm,
            sg.predicted_final AS predictedFinal,
            sg.predicted_average AS predictedAverage,
            sg.comment AS comment,
            c.name AS className,
            c.term AS term
        FROM subject_grades sg
        JOIN students s ON sg.student_id = s.user_id
        JOIN users u ON s.user_id = u.user_id
        JOIN class_subjects cs ON sg.class_subject_id = cs.class_subject_id
        JOIN subjects subj ON cs.subject_id = subj.subject_id
        JOIN classes c ON cs.class_id = c.class_id
        WHERE s.user_id = :studentId
        """, nativeQuery = true)
    List<Object[]> findGradesByStudentId(@Param("studentId") Long studentId);

    Optional<Student> findByEmail(String email);
    // lấy student theo id
    Student findByUserId(Long studentId);

    // lấy ds student theo classId (nếu cần)
    List<Student> findBySchool_SchoolId(Long schoolId);
    boolean existsByStudentCode(String studentCode);
    boolean existsByEmail(String email);
    boolean existsByNumberPhone(String numberPhone);
    // Lấy danh sách sinh viên theo classId (dựa vào bảng Enrollment)
    @Query("SELECT e.student FROM Enrollment e WHERE e.clazz.classId = :classId")
    List<Student> findStudentsByClassId(@Param("classId") Long classId);

    // Lấy danh sách sinh viên theo schoolId
    @Query("SELECT s FROM Student s WHERE s.school.schoolId = :schoolId")
    List<Student> findStudentsBySchoolId(@Param("schoolId") Long schoolId);

}
