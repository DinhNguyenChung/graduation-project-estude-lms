package org.example.estudebackendspring.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.config.JwtTokenUtil;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.Admin;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.exception.InvalidPasswordException;
import org.example.estudebackendspring.exception.InvalidStudentCodeException;
import org.example.estudebackendspring.service.AuthService;
import org.example.estudebackendspring.service.JwtBlacklistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtBlacklistService jwtBlacklistService;

    @PostMapping("/login-student")
    public ResponseEntity<?> loginStudent(@RequestBody LoginRequest loginRequest) {
        try {
            Student student = authService.loginByCode(loginRequest.getUsername(), loginRequest.getPassword());
            // JWT
            String token = jwtTokenUtil.generateToken(student.getStudentCode());
            return ResponseEntity.ok(new LoginResponse(true, "Login successful", student, token));
        } catch (InvalidStudentCodeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse(false, "Invalid student code", null,null));
        } catch (InvalidPasswordException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse(false, "Invalid password", null, null));
        }
    }
    @PostMapping("/login-teacher")
    public ResponseEntity<?> loginTeacher(@RequestBody LoginRequest loginRequest) {
        try {
            Teacher teacher = authService.loginByCodeByTeacher(loginRequest.getUsername(), loginRequest.getPassword());
            // JWT
            String token = jwtTokenUtil.generateToken(teacher.getTeacherCode());
            return ResponseEntity.ok(new LoginResponse(true, "Login successful", teacher, token));
        } catch (InvalidStudentCodeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse(false, "Invalid teacher code", null,null));
        } catch (InvalidPasswordException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse(false, "Invalid password", null, null));
        }
    }
    @PostMapping("/login-admin")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginRequest loginRequest) {
        try {
            Admin admin = authService.loginByCodeByAdmin(loginRequest.getUsername(), loginRequest.getPassword());
            // JWT
            String token = jwtTokenUtil.generateToken(admin.getAdminCode());
            return ResponseEntity.ok(new LoginResponse(true, "Login successful", admin, token));
        } catch (InvalidStudentCodeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse(false, "Invalid teacher code", null,null));
        } catch (InvalidPasswordException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse(false, "Invalid password", null, null));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.initiateForgotPassword(request.getEmail());
        return ResponseEntity.ok(new ApiResponse(true, "OTP has been sent to your email"));
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(new ApiResponse(true, "OTP is valid"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse(true, "Password reset successful"));
    }
    @PutMapping("/update-password")
    public ResponseEntity<UpdatePasswordResponse> updatePassword(
            @RequestBody UpdatePasswordRequest request,
            Authentication authentication,                     // có thể null nếu chưa auth
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String loginCode = null;

        if (authentication != null && authentication.isAuthenticated()) {
            loginCode = authentication.getName();
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // fallback: parse token trực tiếp (nếu bạn cho phép)
            String token = authHeader.substring(7);
            if (!jwtTokenUtil.validateToken(token)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }
            loginCode = jwtTokenUtil.getLoginCodeFromToken(token);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String newToken = authService.updatePassword(loginCode, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(new UpdatePasswordResponse(true, "Password updated successfully", newToken));
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new SimpleResponse(false, "Authorization header missing"));
        }

        String token = authHeader.substring(7).trim();

        // Validate token first (throws or returns false)
        if (!jwtTokenUtil.validateToken(token)) {
            return ResponseEntity.status(401).body(new SimpleResponse(false, "Invalid or expired token"));
        }

        // Add to blacklist
        jwtBlacklistService.blacklistToken(token);

        return ResponseEntity.ok(new SimpleResponse(true, "Logged out successfully"));
    }

    // Logout DTO for responses
    private record SimpleResponse(boolean success, String message) {}


}