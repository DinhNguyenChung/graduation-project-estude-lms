package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.enums.DifficultyLevel;
import org.example.estudebackendspring.enums.QuestionType;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;
    
    @Column(columnDefinition = "TEXT")
    private String questionText;
    
    private Float points;
    
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Column(name = "question_order")
    private Integer questionOrder;

    private String attachmentUrl;
    
    /**
     * Chủ đề mà câu hỏi này thuộc về
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    @JsonIgnore
    private Topic topic;
    
    /**
     * Mức độ khó của câu hỏi
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DifficultyLevel difficultyLevel;
    
    /**
     * Đánh dấu câu hỏi có thuộc ngân hàng đề không
     * true = trong question bank (có thể tái sử dụng)
     * false = trong assignment cụ thể
     */
    @Column(nullable = false)
    private Boolean isQuestionBank = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    @JsonIgnore
    private Assignment assignment;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<QuestionOption> options;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore  // Prevent N+1 query when serializing questions
    private List<Answer> answers;
}
