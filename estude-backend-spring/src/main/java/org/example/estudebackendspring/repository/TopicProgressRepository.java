package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.TopicProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicProgressRepository extends JpaRepository<TopicProgress, Long> {
    
    /**
     * Lấy tất cả progress records của một học sinh theo môn học
     */
    @Query("SELECT tp FROM TopicProgress tp " +
           "WHERE tp.student.userId = :studentId " +
           "AND tp.topic.subject.subjectId = :subjectId " +
           "ORDER BY tp.recordedAt DESC")
    List<TopicProgress> findByStudentAndSubject(
        @Param("studentId") Long studentId, 
        @Param("subjectId") Long subjectId
    );
    
    /**
     * Lấy lịch sử của một topic cụ thể của học sinh
     */
    List<TopicProgress> findByStudent_UserIdAndTopic_TopicIdOrderByRecordedAtDesc(
        Long studentId, 
        Long topicId
    );
    
    /**
     * Lấy progress records của một submission
     */
    List<TopicProgress> findBySubmission_SubmissionId(Long submissionId);
    
    /**
     * Tính accuracy trung bình của học sinh cho từng topic
     */
    @Query("SELECT tp.topic.topicId, AVG(tp.accuracyRate) " +
           "FROM TopicProgress tp " +
           "WHERE tp.student.userId = :studentId " +
           "AND tp.topic.subject.subjectId = :subjectId " +
           "GROUP BY tp.topic.topicId")
    List<Object[]> getAverageAccuracyByTopic(
        @Param("studentId") Long studentId,
        @Param("subjectId") Long subjectId
    );
    
    /**
     * Lấy các topics yếu (accuracy < threshold)
     */
    @Query("SELECT tp FROM TopicProgress tp " +
           "WHERE tp.student.userId = :studentId " +
           "AND tp.topic.subject.subjectId = :subjectId " +
           "AND tp.accuracyRate < :threshold " +
           "ORDER BY tp.accuracyRate ASC, tp.recordedAt DESC")
    List<TopicProgress> findWeakTopics(
        @Param("studentId") Long studentId,
        @Param("subjectId") Long subjectId,
        @Param("threshold") Float threshold
    );
    
    /**
     * Lấy progress record mới nhất của từng topic
     */
    @Query("SELECT tp FROM TopicProgress tp " +
           "WHERE tp.student.userId = :studentId " +
           "AND tp.topic.subject.subjectId = :subjectId " +
           "AND tp.recordedAt = (" +
           "    SELECT MAX(tp2.recordedAt) FROM TopicProgress tp2 " +
           "    WHERE tp2.student.userId = tp.student.userId " +
           "    AND tp2.topic.topicId = tp.topic.topicId" +
           ")")
    List<TopicProgress> findLatestProgressByTopic(
        @Param("studentId") Long studentId,
        @Param("subjectId") Long subjectId
    );
}
