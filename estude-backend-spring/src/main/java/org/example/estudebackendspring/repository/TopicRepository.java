package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Topic;
import org.example.estudebackendspring.enums.GradeLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    
    /**
     * Lấy tất cả topics theo môn học với EAGER FETCH subject, sắp xếp theo orderIndex
     */
    @Query("SELECT t FROM Topic t " +
           "LEFT JOIN FETCH t.subject s " +
           "WHERE s.subjectId = :subjectId " +
           "ORDER BY t.orderIndex ASC")
    List<Topic> findBySubject_SubjectIdOrderByOrderIndexAsc(@Param("subjectId") Long subjectId);
    
    /**
     * Lấy topics theo môn học và tập sách (volume) với EAGER FETCH
     * Ví dụ: Lấy tất cả topics của Toán Tập 1
     */
    @Query("SELECT t FROM Topic t " +
           "LEFT JOIN FETCH t.subject s " +
           "WHERE s.subjectId = :subjectId AND t.volume = :volume " +
           "ORDER BY t.orderIndex ASC")
    List<Topic> findBySubject_SubjectIdAndVolumeOrderByOrderIndexAsc(
        @Param("subjectId") Long subjectId, @Param("volume") Integer volume);
    
    /**
     * Lấy topics theo môn học, khối và tập với EAGER FETCH
     * Ví dụ: Lấy tất cả topics của môn Toán, khối 10, tập 1
     */
    @Query("SELECT t FROM Topic t " +
           "LEFT JOIN FETCH t.subject s " +
           "WHERE s.subjectId = :subjectId " +
           "AND t.gradeLevel = :gradeLevel " +
           "AND t.volume = :volume " +
           "ORDER BY t.orderIndex ASC")
    List<Topic> findBySubject_SubjectIdAndGradeLevelAndVolumeOrderByOrderIndexAsc(
        @Param("subjectId") Long subjectId, 
        @Param("gradeLevel") GradeLevel gradeLevel, 
        @Param("volume") Integer volume);
    
    /**
     * Lấy topics theo môn học và khối với EAGER FETCH
     * Ví dụ: Lấy tất cả topics của môn Toán khối 10
     */
    @Query("SELECT t FROM Topic t " +
           "LEFT JOIN FETCH t.subject s " +
           "WHERE s.subjectId = :subjectId " +
           "AND t.gradeLevel = :gradeLevel " +
           "ORDER BY t.volume ASC, t.orderIndex ASC")
    List<Topic> findBySubject_SubjectIdAndGradeLevelOrderByVolumeAscOrderIndexAsc(
        @Param("subjectId") Long subjectId, 
        @Param("gradeLevel") GradeLevel gradeLevel);
    
    /**
     * Lấy tất cả grade levels có trong một môn học
     * Dùng để hiển thị danh sách khối có sẵn cho môn đó
     */
    @Query("SELECT DISTINCT t.gradeLevel FROM Topic t WHERE t.subject.subjectId = :subjectId ORDER BY t.gradeLevel")
    List<GradeLevel> findDistinctGradeLevelsBySubjectId(@Param("subjectId") Long subjectId);
    
    /**
     * Lấy tất cả volumes của một môn học và khối
     * Dùng để hiển thị danh sách tập có sẵn cho môn + khối
     */
    @Query("SELECT DISTINCT t.volume FROM Topic t WHERE t.subject.subjectId = :subjectId AND t.gradeLevel = :gradeLevel ORDER BY t.volume")
    List<Integer> findDistinctVolumesBySubjectIdAndGradeLevel(
        @Param("subjectId") Long subjectId,
        @Param("gradeLevel") GradeLevel gradeLevel);
    
    /**
     * Lấy topics theo chương
     */
    List<Topic> findBySubject_SubjectIdAndChapterOrderByOrderIndexAsc(Long subjectId, String chapter);
    
    /**
     * Lấy topics theo chương và tập
     */
    List<Topic> findBySubject_SubjectIdAndChapterAndVolumeOrderByOrderIndexAsc(
        Long subjectId, String chapter, Integer volume);
    
    /**
     * Đếm số câu hỏi trong question bank của mỗi topic
     */
    @Query("SELECT t.topicId, COUNT(q) FROM Topic t " +
           "LEFT JOIN t.questions q " +
           "WHERE t.subject.subjectId = :subjectId AND q.isQuestionBank = true " +
           "GROUP BY t.topicId")
    List<Object[]> countQuestionsByTopic(@Param("subjectId") Long subjectId);
    
    /**
     * Đếm số câu hỏi theo topic và volume
     */
    @Query("SELECT t.topicId, COUNT(q) FROM Topic t " +
           "LEFT JOIN t.questions q " +
           "WHERE t.subject.subjectId = :subjectId AND t.volume = :volume " +
           "AND q.isQuestionBank = true " +
           "GROUP BY t.topicId")
    List<Object[]> countQuestionsByTopicAndVolume(
        @Param("subjectId") Long subjectId, 
        @Param("volume") Integer volume);
    
    /**
     * Kiểm tra tên topic đã tồn tại chưa
     */
    boolean existsByNameAndSubject_SubjectId(String name, Long subjectId);
}
