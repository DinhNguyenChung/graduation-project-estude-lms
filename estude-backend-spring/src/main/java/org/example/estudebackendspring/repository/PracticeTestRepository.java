package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.PracticeTest;
import org.example.estudebackendspring.enums.TestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PracticeTestRepository extends JpaRepository<PracticeTest, Long> {
    
    /**
     * Lấy tất cả practice tests của một học sinh
     */
    List<PracticeTest> findByStudent_UserIdOrderByCreatedAtDesc(Long studentId);
    
    /**
     * Lấy practice tests của học sinh theo môn học
     */
    @Query("SELECT pt FROM PracticeTest pt " +
           "WHERE pt.student.userId = :studentId " +
           "AND pt.subject.subjectId = :subjectId " +
           "ORDER BY pt.createdAt DESC")
    List<PracticeTest> findByStudentAndSubject(
        @Param("studentId") Long studentId,
        @Param("subjectId") Long subjectId
    );
    
    /**
     * Lấy practice tests chưa làm (chưa có submission)
     */
    @Query("SELECT pt FROM PracticeTest pt " +
           "WHERE pt.student.userId = :studentId " +
           "AND pt.submission IS NULL " +
           "ORDER BY pt.createdAt DESC")
    List<PracticeTest> findPendingTestsByStudent(@Param("studentId") Long studentId);
    
    /**
     * Lấy practice tests đã hoàn thành
     */
    @Query("SELECT pt FROM PracticeTest pt " +
           "WHERE pt.student.userId = :studentId " +
           "AND pt.submission IS NOT NULL " +
           "ORDER BY pt.createdAt DESC")
    List<PracticeTest> findCompletedTestsByStudent(@Param("studentId") Long studentId);
    
    /**
     * Lấy practice tests theo loại
     */
    List<PracticeTest> findByStudent_UserIdAndTestTypeOrderByCreatedAtDesc(
        Long studentId, 
        TestType testType
    );
    
    /**
     * Xóa các tests đã hết hạn và chưa làm
     */
    @Query("SELECT pt FROM PracticeTest pt " +
           "WHERE pt.expiresAt < :now " +
           "AND pt.submission IS NULL")
    List<PracticeTest> findExpiredTests(@Param("now") LocalDateTime now);
    
    /**
     * Đếm số bài test của học sinh
     */
    long countByStudent_UserId(Long studentId);
    
    /**
     * Đếm số bài test đã hoàn thành
     */
    @Query("SELECT COUNT(pt) FROM PracticeTest pt " +
           "WHERE pt.student.userId = :studentId " +
           "AND pt.submission IS NOT NULL")
    long countCompletedTestsByStudent(@Param("studentId") Long studentId);
}
