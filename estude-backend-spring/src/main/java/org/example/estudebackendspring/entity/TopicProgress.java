package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity theo dõi tiến độ học tập của học sinh theo từng chủ đề
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "topic_progress", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"student_id", "topic_id", "submission_id"})
       })
public class TopicProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long progressId;
    
    /**
     * Học sinh
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private Student student;
    
    /**
     * Chủ đề
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    @JsonIgnore
    private Topic topic;
    
    /**
     * Submission liên quan (nếu có)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    @JsonIgnore
    private Submission submission;
    
    /**
     * Tổng số câu hỏi của topic trong lần làm bài này
     */
    @Column(nullable = false)
    private Integer totalQuestions;
    
    /**
     * Số câu trả lời đúng
     */
    @Column(nullable = false)
    private Integer correctAnswers;
    
    /**
     * Tỷ lệ chính xác (0.0 -> 1.0)
     * Ví dụ: 0.6 = 60%
     */
    @Column(nullable = false)
    private Float accuracyRate;
    
    /**
     * Thời gian ghi nhận
     */
    @Column(nullable = false)
    private LocalDateTime recordedAt;
}
