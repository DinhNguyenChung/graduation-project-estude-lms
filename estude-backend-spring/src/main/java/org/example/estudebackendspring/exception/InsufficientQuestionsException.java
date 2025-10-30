package org.example.estudebackendspring.exception;

import lombok.Getter;

import java.util.Map;

/**
 * Exception thrown when there aren't enough questions available in the question bank
 */
@Getter
public class InsufficientQuestionsException extends RuntimeException {
    private final Map<String, Object> details;
    
    public InsufficientQuestionsException(Long topicId, String topicName, 
                                         int requested, int available, String difficulty) {
        super("Không tìm đủ câu hỏi cho các topics đã chọn");
        this.details = Map.of(
            "topicId", topicId,
            "topicName", topicName,
            "requested", requested,
            "available", available,
            "difficulty", difficulty
        );
    }
    
    public String getErrorCode() {
        return "INSUFFICIENT_QUESTIONS";
    }
}
