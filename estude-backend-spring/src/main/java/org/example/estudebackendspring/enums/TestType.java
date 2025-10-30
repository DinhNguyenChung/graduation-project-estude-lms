package org.example.estudebackendspring.enums;

/**
 * Loại bài test luyện tập
 */
public enum TestType {
    /**
     * Học sinh tự tạo để đánh giá kiến thức
     */
    SELF_ASSESSMENT,
    
    /**
     * AI sinh ra tự động dựa trên các topics yếu
     */
    AI_GENERATED,
    
    /**
     * Giáo viên tạo và giao cho học sinh
     */
    TEACHER_ASSIGNED
}
