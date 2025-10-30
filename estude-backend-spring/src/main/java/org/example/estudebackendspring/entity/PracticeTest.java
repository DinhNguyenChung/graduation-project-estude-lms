package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.enums.TestType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho bài test luyện tập (practice test)
 * Được tạo động dựa trên việc học sinh chọn topics và số câu hỏi
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "practice_tests")
public class PracticeTest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testId;
    
    /**
     * Tiêu đề bài test: "Kiểm tra Toán 10 - Mệnh đề & Tập hợp"
     */
    @Column(nullable = false)
    private String title;
    
    /**
     * Môn học
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    @JsonIgnore
    private Subject subject;
    
    /**
     * Học sinh (bài test được tạo cho ai)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private Student student;
    
    /**
     * Loại bài test
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestType testType;
    
    /**
     * Thời gian tạo
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Thời gian hết hạn (nếu có)
     */
    private LocalDateTime expiresAt;
    
    /**
     * Tổng số câu hỏi
     */
    @Column(nullable = false)
    private Integer totalQuestions;
    
    /**
     * Thời gian làm bài (phút)
     */
    private Integer timeLimit;
    
    /**
     * Các topics được chọn để tạo câu hỏi
     */
    @ManyToMany
    @JoinTable(
        name = "practice_test_topics",
        joinColumns = @JoinColumn(name = "test_id"),
        inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    private List<Topic> selectedTopics = new ArrayList<>();
    
    /**
     * Các câu hỏi được random từ question bank
     */
    @ManyToMany
    @JoinTable(
        name = "practice_test_questions",
        joinColumns = @JoinColumn(name = "test_id"),
        inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions = new ArrayList<>();
    
    /**
     * Submission nếu học sinh đã làm bài
     */
    @OneToOne(mappedBy = "practiceTest", fetch = FetchType.LAZY)
    @JsonIgnore
    private Submission submission;
}
