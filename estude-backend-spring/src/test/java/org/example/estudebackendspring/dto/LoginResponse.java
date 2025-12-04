package org.example.estudebackendspring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO cho Login Response
 * Matches main codebase: LoginResponse(boolean success, String message, Object data, String token)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    private boolean success;
    private String message;
    @JsonProperty("data")
    private Object data;
    private String token;

    public LoginResponse() {}

    public LoginResponse(boolean success, String message, Object data, String token) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.token = token;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
