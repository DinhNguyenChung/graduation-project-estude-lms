package org.example.estudebackendspring.exception;

public class SubmissionLimitExceededException extends RuntimeException {
    public SubmissionLimitExceededException(String message) {
        super(message);
    }
}
