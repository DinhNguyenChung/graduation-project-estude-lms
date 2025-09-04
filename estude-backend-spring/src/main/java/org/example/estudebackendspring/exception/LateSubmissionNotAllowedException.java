package org.example.estudebackendspring.exception;

public class LateSubmissionNotAllowedException extends RuntimeException {
    public LateSubmissionNotAllowedException(String message) {
        super(message);
    }
}
