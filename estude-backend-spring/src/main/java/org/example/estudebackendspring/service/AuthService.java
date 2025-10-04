package org.example.estudebackendspring.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.config.JwtTokenUtil;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.exception.InvalidPasswordException;
import org.example.estudebackendspring.exception.InvalidStudentCodeException;
import org.example.estudebackendspring.repository.*;
import org.example.estudebackendspring.until.EmailUtil;
import org.example.estudebackendspring.until.OtpUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final EmailUtil emailUtil;
    private final PasswordResetTokenRepository tokenRepository;
    // Thời hạn reset token (phút)
    private final long OTP_EXPIRE_MINUTES = 15L;
    private final long MIN_SECONDS_BETWEEN_REQUESTS = 60L; // rate-limit cơ bản
    private final OtpUtil otpUtil;
    private final TeacherRepository teacherRepository;
    private  final  AdminRepository adminRepository;

    //    public Optional<User> loginByEmailOrPhone(String emailOrPhone, String password) {
//        return userRepository.findByEmail(emailOrPhone)
//                .or(() -> userRepository.findByNumberPhone(emailOrPhone))
//                .filter(u -> u.getPassword().equals(password));
//    }
    public Student loginByCode(String loginCode, String password) {
        Student student = studentRepository.findByStudentCode(loginCode)
                .orElseThrow(() -> new InvalidStudentCodeException("Mã không hợp lệ!"));

        if (!passwordEncoder.matches(password, student.getPassword())) {
            throw new InvalidPasswordException("Mật khẩu không hợp lệ!");
        }

        return student;
    }
    public Teacher loginByCodeByTeacher(String loginCode, String password) {
        Teacher teacher = teacherRepository.findByTeacherCode(loginCode)
                .orElseThrow(() -> new InvalidStudentCodeException("Mã không hợp lệ!"));

        if (!passwordEncoder.matches(password, teacher.getPassword())) {
            throw new InvalidPasswordException("Mật khẩu không hợp lệ!");
        }

        return teacher;
    }
    public Admin loginByCodeByAdmin(String loginCode, String password) {
        Admin admin = adminRepository.findByAdminCode(loginCode)
                .orElseThrow(() -> new InvalidStudentCodeException("Mã không hợp lệ!"));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new InvalidPasswordException("Mật khẩu không hợp lệ!");
        }

        return admin;
    }
    /**
     * Gửi OTP đến email (lưu token vào DB)
     */
    @Transactional
    public void initiateForgotPassword(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email là bắt buộc");
        }

        email = email.trim();

        // tìm user theo email (Student, Teacher, Admin)
        Student student = studentRepository.findByEmail(email).orElse(null);
        Teacher teacher = (student == null) ? teacherRepository.findByEmail(email).orElse(null) : null;
        Admin admin = (student == null && teacher == null) ? adminRepository.findByEmail(email).orElse(null) : null;

        if (student == null && teacher == null && admin == null) {
            // Nếu bạn muốn tránh leak user existence, có thể return 200 OK tại đây thay vì throw error
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy người dùng");
        }

        Object user = (student != null) ? student : (teacher != null ? teacher : admin);

        // Rate-limit: kiểm tra token mới nhất của user
        Optional<PasswordResetToken> last = tokenRepository.findLatestActiveByEmailOrPhone(email);
        if (last.isPresent() && !last.get().isUsed() &&
                last.get().getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(MIN_SECONDS_BETWEEN_REQUESTS))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Vui lòng đợi trước khi yêu cầu OTP khác");
        }

        // tạo OTP 6 chữ số
        String otp = otpUtil.generateOtp();

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(otp);
        token.setEmailOrPhone(email);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES));
        token.setUsed(false);
        token.setCreatedAt(LocalDateTime.now());

        tokenRepository.save(token);

        try {
            emailUtil.sendOtpEmail(email, otp);
        } catch (MessagingException e) {
            tokenRepository.delete(token); // rollback nhẹ nếu gửi mail fail
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP email");
        }
    }


    /**
     * Kiểm tra OTP hợp lệ (chưa dùng, chưa expired)
     */
    @Transactional(readOnly = true)
    public void verifyOtp(String email, String otp) {
        if (email == null || otp == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and OTP are required");
        }

        Optional<PasswordResetToken> maybe = tokenRepository.findLatestActiveByEmailOrPhone(email.trim());
        if (maybe.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP or email");
        }

        PasswordResetToken token = maybe.get();

        if (token.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }

        if (!token.getToken().equals(otp.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        // Nếu bạn muốn đánh dấu đã verified (nhưng chưa used), bạn có thể add flag; ở đây ta giữ token chưa used
    }

    /**
     * Reset password bằng email + otp
     */
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        if (email == null || otp == null || newPassword == null || newPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email, OTP and new password are required");
        }

        Optional<PasswordResetToken> maybe = tokenRepository.findLatestActiveByEmailOrPhone(email.trim());
        if (maybe.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP or email");
        }

        PasswordResetToken token = maybe.get();

        if (token.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }

        if (!token.getToken().equals(otp.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        // tìm user theo email (Student, Teacher, Admin)
        Student student = studentRepository.findByEmail(email).orElse(null);
        Teacher teacher = (student == null) ? teacherRepository.findByEmail(email).orElse(null) : null;
        Admin admin = (student == null && teacher == null) ? adminRepository.findByEmail(email).orElse(null) : null;

        if (student != null) {
            student.setPassword(passwordEncoder.encode(newPassword));
            studentRepository.save(student);
        } else if (teacher != null) {
            teacher.setPassword(passwordEncoder.encode(newPassword));
            teacherRepository.save(teacher);
        } else if (admin != null) {
            admin.setPassword(passwordEncoder.encode(newPassword));
            adminRepository.save(admin);
        } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found for this email");
        }

        // mark token as used (chỉ được dùng 1 lần)
        token.setUsed(true);
        tokenRepository.save(token);
    }

    // Cập nhật mật khẩu
    /**
     * Update password for an authenticated user identified by loginCode (studentCode).
     * Returns a new JWT token after successful update.
     */
    @Transactional
    public String updatePassword(String loginCode, String currentPassword, String newPassword) {
        if (currentPassword == null || newPassword == null || newPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current and new password are required");
        }

        // 1. Try Student
        Optional<Student> optStudent = studentRepository.findByStudentCode(loginCode);
        if (optStudent.isPresent()) {
            Student student = optStudent.get();
            return updatePasswordForUser(student.getStudentCode(),student.getUserId(), student.getPassword(),
                    p -> { student.setPassword(p); studentRepository.save(student); }, currentPassword, newPassword);
        }

        // 2. Try Teacher
        Optional<Teacher> optTeacher = teacherRepository.findByTeacherCode(loginCode);
        if (optTeacher.isPresent()) {
            Teacher teacher = optTeacher.get();
            return updatePasswordForUser(teacher.getTeacherCode(),teacher.getUserId(), teacher.getPassword(),
                    p -> { teacher.setPassword(p); teacherRepository.save(teacher); }, currentPassword, newPassword);
        }

        // 3. Try Admin
        Optional<Admin> optAdmin = adminRepository.findByAdminCode(loginCode);
        if (optAdmin.isPresent()) {
            Admin admin = optAdmin.get();
            return updatePasswordForUser(admin.getAdminCode(),admin.getUserId(), admin.getPassword(),
                    p -> { admin.setPassword(p); adminRepository.save(admin); }, currentPassword, newPassword);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
    }

    /**
     * Helper để tránh lặp code: kiểm tra current password, check new != old, cập nhật và trả token.
     *
     * @param tokenSubject giá trị sẽ dùng làm subject trong JWT (vd studentCode/teacherCode/adminCode)
     * @param currentHashedPassword hashed password hiện tại (lấy từ entity)
     * @param saveAction action nhận mật khẩu đã mã hoá để lưu entity tương ứng
     * @param currentPassword mật khẩu hiện tại do user nhập
     * @param newPassword mật khẩu mới do user nhập
     * @return JWT mới
     */
    private String updatePasswordForUser(
            String tokenSubject,
            Long userId,
            String currentHashedPassword,
            java.util.function.Consumer<String> saveAction,
            String currentPassword,
            String newPassword
    ) {
        // Check current password
        if (!passwordEncoder.matches(currentPassword, currentHashedPassword)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }

        // Prevent reusing same password
        if (passwordEncoder.matches(newPassword, currentHashedPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from current password");
        }

        // Encode and save
        String newHashed = passwordEncoder.encode(newPassword);
        saveAction.accept(newHashed);

        // Optionally: update a "passwordLastChangedAt" field on user here to invalidate old JWTs (recommended)
        // Trả JWT mới
        return jwtTokenUtil.generateToken(tokenSubject,userId);
    }




}
