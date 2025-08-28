package org.example.estudebackendspring.exception;

public class InvalidStudentCodeException extends RuntimeException {
    public InvalidStudentCodeException(String message) {
        super(message);
    }
    public InvalidStudentCodeException() {
      super("Mã sinh viên không tồn tại");
    }
}
