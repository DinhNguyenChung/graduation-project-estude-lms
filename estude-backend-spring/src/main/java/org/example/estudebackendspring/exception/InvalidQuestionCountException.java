package org.example.estudebackendspring.exception;

import lombok.Getter;

import java.util.Map;

/**
 * Exception thrown when requested number of questions is less than number of topics
 */
@Getter
public class InvalidQuestionCountException extends RuntimeException {
    private final Map<String, Object> details;
    private final String errorCode;
    
    // Constructor cho trường hợp cơ bản: numQuestions < numTopics
    public InvalidQuestionCountException(int numQuestions, int numTopics) {
        super("Số câu hỏi phải lớn hơn hoặc bằng số topics");
        this.details = Map.of(
            "numQuestions", numQuestions,
            "numTopics", numTopics,
            "minRequired", numTopics
        );
        this.errorCode = "INVALID_QUESTION_COUNT";
    }
    
    // Constructor cho validation phức tạp hơn (ví dụ: mixed difficulty)
    public InvalidQuestionCountException(String message) {
        super(message);
        this.details = Map.of();
        this.errorCode = "INVALID_QUESTION_COUNT";
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
