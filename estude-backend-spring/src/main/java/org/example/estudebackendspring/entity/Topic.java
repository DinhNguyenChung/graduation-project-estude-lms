package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.enums.GradeLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho chủ đề/bài học trong sách giáo khoa
 * Ví dụ: "Mệnh đề", "Tập hợp và các phép toán trên tập hợp"
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "topics")
public class Topic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long topicId;
    
    /**
     * Tên chủ đề: "Mệnh đề"
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * Mô tả chi tiết về chủ đề
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Chương mục: "CHƯƠNG I: MỆNH ĐỀ VÀ TẬP HỢP"
     */
    @Column(length = 200)
    private String chapter;
    
    /**
     * Số thứ tự bài học: 1, 2, 3...
     */
    private Integer orderIndex;
    
    /**
     * Khối học: GRADE_10, GRADE_11, GRADE_12
     * Dùng để phân biệt topic thuộc khối nào
     * Ví dụ: Topic "Mệnh đề" thuộc khối 10
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "grade_level")
    private GradeLevel gradeLevel;
    
    /**
     * Tập sách: 1, 2, 3... (Tập 1, Tập 2)
     * Dùng để phân biệt các chủ đề thuộc tập nào trong năm học
     * Ví dụ: Toán 10 có Tập 1 (7 chương đầu) và Tập 2 (5 chương sau)
     */
    @Column(name = "volume")
    private Integer volume;
    
    /**
     * Môn học (Toán, Vật lý, Hóa học...)
     * Note: Subject không còn chứa gradeLevel/volume, chỉ chứa tên môn
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    @JsonIgnore
    private Subject subject;
    
    /**
     * Các câu hỏi thuộc chủ đề này
     */
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Question> questions = new ArrayList<>();
    
    /**
     * Tracking kết quả học tập của học sinh theo topic này
     */
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TopicProgress> topicProgresses = new ArrayList<>();
}
